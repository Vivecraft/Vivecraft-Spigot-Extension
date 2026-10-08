package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.GameModeHelper;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class GameModeHelper_26_1 implements GameModeHelper {

    protected ReflectionField ServerPlayerGamemode_hasDelayedDestroy;
    protected ReflectionField ServerPlayerGamemode_isDestroyingBlock;
    protected ReflectionField ServerPlayerGamemode_destroyPos;
    protected ReflectionField ServerPlayerGamemode_player;
    protected ReflectionField ServerPlayerGamemode_lastSentState;

    protected ReflectionField ServerPlayer_gameMode;

    protected ReflectionMethod Level_getBlockStateIfLoaded_paper;

    public GameModeHelper_26_1() {
        this.init();
    }

    protected void init() {
        this.ServerPlayerGamemode_isDestroyingBlock = ReflectionField.getRaw(ServerPlayerGameMode.class,
            "isDestroyingBlock");
        this.ServerPlayerGamemode_hasDelayedDestroy = ReflectionField.getRaw(ServerPlayerGameMode.class,
            "hasDelayedDestroy");
        this.ServerPlayerGamemode_destroyPos = ReflectionField.getRaw(ServerPlayerGameMode.class,
            "destroyPos");
        this.ServerPlayerGamemode_player = ReflectionField.getRaw(ServerPlayerGameMode.class, "player");
        this.ServerPlayerGamemode_lastSentState = ReflectionField.getRaw(ServerPlayerGameMode.class,
            "lastSentState");

        this.ServerPlayer_gameMode = ReflectionField.getRaw(ServerPlayer.class, "gameMode");

        this.Level_getBlockStateIfLoaded_paper = ReflectionMethod.getRaw(Level.class, "getBlockStateIfLoaded", false,
            BlockPos.class);
    }

    @Override
    public void wrapTick(Object gameMode, Runnable tick) {
        this.wrapTick((ServerPlayerGameMode) gameMode, tick);
    }

    private void wrapTick(ServerPlayerGameMode gameMode, Runnable tick) {
        // if there is a delayed destroy, run tick normally
        if ((boolean) this.ServerPlayerGamemode_hasDelayedDestroy.get(gameMode)) {
            tick.run();
            return;
        }

        ServerPlayer player = (ServerPlayer) this.ServerPlayerGamemode_player.get(gameMode);
        VivePlayer vivePlayer = ViveMain.NMS.getVRPlayer(player);

        boolean isDestroyingBlock = (boolean) this.ServerPlayerGamemode_isDestroyingBlock.get(gameMode);
        boolean destroySet = false;

        // doesn't matter if they are currently in vr, if they hit roomscale do not send updates on tick
        if (vivePlayer != null && vivePlayer.lastHitRoomscale && isDestroyingBlock) {
            // potentially ticking roomscale hit effects
            // only do if the block is still there
            BlockPos destroyPos = (BlockPos) this.ServerPlayerGamemode_destroyPos.get(gameMode);
            BlockState blockState = this.Level_getBlockStateIfLoaded_paper != null ?
                (BlockState) this.Level_getBlockStateIfLoaded_paper.invoke(player.level(), destroyPos) :
                player.level().getBlockState(destroyPos);

            if (blockState != null && !blockState.isAir()) {
                // block there can do effects, and disable vanilla effects
                destroySet = true;
                this.ServerPlayerGamemode_isDestroyingBlock.set(gameMode, false);

                // destroy progress update
                sendBreakProgress(gameMode, player, destroyPos, vivePlayer.roomscaleHitProgress);

                // destroy particles and sound for 26.3+
                doBlockBreakEffects(vivePlayer, player, gameMode, destroyPos);
            }
        }

        // run vanilla tick
        tick.run();

        if (destroySet) {
            this.ServerPlayerGamemode_isDestroyingBlock.set(gameMode, isDestroyingBlock);
        }
    }

    private void sendBreakProgress(ServerPlayerGameMode gameMode, ServerPlayer player, BlockPos pos, float progress) {
        int lastState = (int) this.ServerPlayerGamemode_lastSentState.get(gameMode);
        // max of 9, since 1.0 progress removes the break overlay again
        int newState = Math.min(9, (int) (progress * 10F));
        if (newState != lastState) {
            player.level().destroyBlockProgress(player.getId(), pos, newState);
            this.ServerPlayerGamemode_lastSentState.set(gameMode, newState);
        }
    }

    protected void doBlockBreakEffects(
        VivePlayer vivePlayer, ServerPlayer player, ServerPlayerGameMode gameMode, BlockPos destroyPos)
    {}

    @Override
    public void postHandleBlockBreakAction(Object gameMode) {
        this.postHandleBlockBreakAction((ServerPlayerGameMode) gameMode);
    }

    private void postHandleBlockBreakAction(ServerPlayerGameMode gameMode) {
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
        ServerPlayer serverPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);

        // check if it is already wrapped
        if (serverPlayer.gameMode instanceof VRServerPlayerGameMode || serverPlayer.gameMode instanceof VRDemoMode) {
            Debug.log("Gamemode of %s is already wrapped.", serverPlayer.getScoreboardName());
            return true;
        }

        if (serverPlayer.gameMode.getClass() == ServerPlayerGameMode.class) {
            Debug.log("Wrapping gamemode of %s with the VR gamemode", serverPlayer.getScoreboardName());
            this.ServerPlayer_gameMode.set(serverPlayer, new VRServerPlayerGameMode(serverPlayer));
            return true;
        } else if (serverPlayer.gameMode.getClass() == DemoMode.class) {
            Debug.log("Wrapping gamemode of %s with the VR Demo gamemode", serverPlayer.getScoreboardName());
            this.ServerPlayer_gameMode.set(serverPlayer, new VRDemoMode(serverPlayer));
            return true;
        } else {
            Debug.log("Gamemode of %s was not wrapped. unknown Gamemode: %s", serverPlayer.getScoreboardName(),
                serverPlayer.gameMode.getClass());
            return false;
        }
    }
}
