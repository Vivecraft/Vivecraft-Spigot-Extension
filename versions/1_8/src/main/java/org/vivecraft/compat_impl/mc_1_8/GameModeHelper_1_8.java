package org.vivecraft.compat_impl.mc_1_8;

import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.GameModeHelper;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class GameModeHelper_1_8 implements GameModeHelper {

    protected ReflectionField ServerPlayerGamemode_hasDelayedDestroy;
    protected ReflectionField ServerPlayerGamemode_isDestroyingBlock;
    protected ReflectionField ServerPlayerGamemode_destroyPos;
    protected ReflectionField ServerPlayerGamemode_player;
    protected ReflectionField ServerPlayerGamemode_lastSentState;

    protected ReflectionField ServerPlayer_gameMode;

    protected ReflectionMethod Level_destroyBlockProgress;
    protected ReflectionMethod Level_getBlockStateIfLoaded_paper;

    protected ReflectionMethod Entity_getId;
    private ReflectionMethod Block_getMaterial;
    private ReflectionField Material_AIR;
    private ReflectionMethod BlockState_getBlock;

    private Class<?> ServerPlayerGameMode;
    private Class<?> DemoMode;

    private ReflectionConstructor VRServerPlayerGameMode_Constructor;
    private ReflectionConstructor VRDemoMode_Constructor;
    private Class<?> VRServerPlayerGameMode;
    private Class<?> VRDemoMode;

    public GameModeHelper_1_8() {
        this.init();
        this.initAir();
    }

    protected void init() {
        this.ServerPlayerGamemode_isDestroyingBlock = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_IS_DESTROYING_BLOCK);
        this.ServerPlayerGamemode_hasDelayedDestroy = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_HAS_DELAYED_DESTROY);
        this.ServerPlayerGamemode_destroyPos = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_DESTROY_POS);
        this.ServerPlayerGamemode_player = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_PLAYER);
        this.ServerPlayerGamemode_lastSentState = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_LAST_SENT_STATE);

        this.ServerPlayer_gameMode = ReflectionField.getField(ServerPlayerMapping.FIELD_GAME_MODE);

        this.Level_getBlockStateIfLoaded_paper = ReflectionMethod.getRaw(
            ClassGetter.getClass(true, LevelMapping.MAPPING),
            new String[]{"getTypeIfLoaded", "getBlockStateIfLoaded"}, false,
            ClassGetter.getClass(true, BlockPosMapping.MAPPING));
        this.Level_destroyBlockProgress = ReflectionMethod.getMethod(
            LevelMapping.METHOD_DESTROY_BLOCK_PROGRESS);

        this.Entity_getId = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_ID);

        this.VRServerPlayerGameMode_Constructor = ReflectionConstructor.getCompat("VRServerPlayerGameMode",
            ClassGetter.getClass(true, ServerPlayerGameModeMapping.MAPPING),
            ClassGetter.getClass(true, ServerPlayerMapping.MAPPING));
        this.VRDemoMode_Constructor = ReflectionConstructor.getCompat("VRDemoMode",
            ClassGetter.getClass(true, ServerPlayerGameModeMapping.MAPPING),
            ClassGetter.getClass(true, ServerPlayerMapping.MAPPING));

        this.VRServerPlayerGameMode = this.VRServerPlayerGameMode_Constructor.constructor.getDeclaringClass();
        this.VRDemoMode = this.VRDemoMode_Constructor.constructor.getDeclaringClass();

        this.ServerPlayerGameMode = ClassGetter.getClass(true, ServerPlayerGameModeMapping.MAPPING);
        this.DemoMode = ClassGetter.getClass(true, DemoModeMapping.MAPPING);
    }

    protected void initAir() {
        this.Material_AIR = ReflectionField.getField(MaterialMapping.FIELD_AIR);
        this.BlockState_getBlock = ReflectionMethod.getMethod(BlockStateMapping.METHOD_GET_BLOCK);
        this.Block_getMaterial = ReflectionMethod.getMethod(BlockMapping.METHOD_GET_MATERIAL);
    }

    @Override
    public void wrapTick(Object gameMode, Runnable tick) {
        // if there is a delayed destroy, run tick normally
        if ((boolean) this.ServerPlayerGamemode_hasDelayedDestroy.get(gameMode)) {
            tick.run();
            return;
        }

        Object nmsPlayer = this.ServerPlayerGamemode_player.get(gameMode);
        VivePlayer vivePlayer = ViveMain.NMS.getVRPlayer(nmsPlayer);

        boolean isDestroyingBlock = (boolean) this.ServerPlayerGamemode_isDestroyingBlock.get(gameMode);
        boolean destroySet = false;

        // doesn't matter if they are currently in vr, if they hit roomscale do not send updates on tick
        if (vivePlayer != null && vivePlayer.lastHitRoomscale && isDestroyingBlock) {
            // potentially ticking roomscale hit effects
            // only do if the block is still there
            Object destroyPos = this.ServerPlayerGamemode_destroyPos.get(gameMode);
            Object level = ViveMain.NMS.getLevel(nmsPlayer);
            Object blockState = this.Level_getBlockStateIfLoaded_paper != null ?
                this.Level_getBlockStateIfLoaded_paper.invoke(level, destroyPos) :
                ViveMain.NMS.getBlockState(level, destroyPos);

            if (blockState != null && !isAir(blockState)) {
                // block there can do effects, and disable vanilla effects
                destroySet = true;
                this.ServerPlayerGamemode_isDestroyingBlock.set(gameMode, false);

                // destroy progress update
                sendBreakProgress(gameMode, nmsPlayer, destroyPos, vivePlayer.roomscaleHitProgress);
            }
        }

        // run vanilla tick
        tick.run();

        if (destroySet) {
            this.ServerPlayerGamemode_isDestroyingBlock.set(gameMode, isDestroyingBlock);
        }
    }

    protected boolean isAir(Object blockState) {
        Object block = this.BlockState_getBlock.invoke(blockState);
        return this.Block_getMaterial.invoke(block) == this.Material_AIR.get();
    }

    private void sendBreakProgress(Object gameMode, Object nmsPlayer, Object blockPos, float progress) {
        int lastState = (int) this.ServerPlayerGamemode_lastSentState.get(gameMode);
        // max of 9, since 1.0 progress removes the break overlay again
        int newState = Math.min(9, (int) (progress * 10F));
        if (newState != lastState) {
            this.Level_destroyBlockProgress.invoke(ViveMain.NMS.getLevel(nmsPlayer),
                this.Entity_getId.invoke(nmsPlayer), blockPos, newState);
            this.ServerPlayerGamemode_lastSentState.set(gameMode, newState);
        }
    }

    @Override
    public void postHandleBlockBreakAction(Object gameMode) {
        VivePlayer vivePlayer = ViveMain.NMS.getVRPlayer(this.ServerPlayerGamemode_player.get(gameMode));
        boolean isDestroyingBlock = (boolean) this.ServerPlayerGamemode_isDestroyingBlock.get(gameMode);
        if (vivePlayer != null) {
            // treat as roomscale hit, if they are destroying a block, and sent the roomscale attack packet for this action
            vivePlayer.lastHitRoomscale = isDestroyingBlock && vivePlayer.isVR() && vivePlayer.isHitRoomscale;
            if (!vivePlayer.lastHitRoomscale) {
                vivePlayer.roomscaleHitProgress = 0;
            }
        }
    }


    @Override
    public boolean modifyGamemode(Player player) {
        // switch out the gamemode with a wrapped one
        Object nmsPlayer = BukkitReflector.getEntityHandle(player);
        Object gameMode = this.ServerPlayer_gameMode.get(nmsPlayer);

        // check if it is already wrapped
        if (this.VRServerPlayerGameMode.isInstance(gameMode) || this.VRDemoMode.isInstance(gameMode)) {
            // don't need to do anything
            return true;
        }

        if (gameMode.getClass() == ServerPlayerGameMode) {
            Debug.log("Wrapping gamemode of %s with the VR gamemode", player.getDisplayName());
            this.ServerPlayer_gameMode.set(nmsPlayer,
                this.VRServerPlayerGameMode_Constructor.newInstance(gameMode, nmsPlayer));
            return true;
        } else if (gameMode.getClass() == DemoMode) {
            Debug.log("Wrapping gamemode of %s with the VR Demo gamemode", player.getDisplayName());
            this.ServerPlayer_gameMode.set(nmsPlayer, this.VRDemoMode_Constructor.newInstance(gameMode, nmsPlayer));
            return true;
        } else {
            Debug.log("Gamemode of %s was not wrapped. unknown Gamemode: %s", player.getDisplayName(),
                gameMode.getClass());
            return false;
        }
    }
}
