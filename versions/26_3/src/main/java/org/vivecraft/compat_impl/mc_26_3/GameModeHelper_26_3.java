package org.vivecraft.compat_impl.mc_26_3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.LevelEvent;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat_impl.mc_26_1.GameModeHelper_26_1;
import org.vivecraft.util.reflection.ReflectionField;

public class GameModeHelper_26_3 extends GameModeHelper_26_1 {

    private ReflectionField ServerPlayerGamemode_destroyDirection;

    public GameModeHelper_26_3() {
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        this.ServerPlayerGamemode_destroyDirection = ReflectionField.getRaw(ServerPlayerGameMode.class,
            "destroyDirection");
    }

    @Override
    protected void doBlockBreakEffects(
        VivePlayer vivePlayer, ServerPlayer player, ServerPlayerGameMode gameMode, BlockPos destroyPos)
    {
        // destroy particles and sound update
        if (vivePlayer.roomscaleHitProgress != vivePlayer.lastRoomscaleHitProgress) {
            vivePlayer.lastRoomscaleHitProgress = vivePlayer.roomscaleHitProgress;
            // send the sound on first tick after hit
            player.level().levelEvent(null, LevelEvent.PARTICLES_AND_SOUND_DESTROY_PROGRESS,
                destroyPos, ((Direction) this.ServerPlayerGamemode_destroyDirection.get(gameMode)).ordinal());
            vivePlayer.roomscaleAttackParticlesRemaining = 3;
        } else if (vivePlayer.roomscaleAttackParticlesRemaining > 0) {
            // send the particles for multiple ticks
            vivePlayer.roomscaleAttackParticlesRemaining--;
            player.level().levelEvent(null, LevelEvent.PARTICLES_DESTROY_PROGRESS,
                destroyPos, ((Direction) this.ServerPlayerGamemode_destroyDirection.get(gameMode)).ordinal());
        }
    }
}
