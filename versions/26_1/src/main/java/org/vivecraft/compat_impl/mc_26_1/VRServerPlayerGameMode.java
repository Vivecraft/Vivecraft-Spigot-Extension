package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.vivecraft.ViveMain;

public class VRServerPlayerGameMode extends ServerPlayerGameMode {
    public VRServerPlayerGameMode(ServerPlayer player) {
        super(player);
        this.setGameModeForPlayer(player.gameMode.getGameModeForPlayer(),
            player.gameMode.getPreviousGameModeForPlayer());
    }

    @Override
    public void tick() {
        ViveMain.MC_MODS.gameModeHelper().wrapTick(this, super::tick);
    }

    @Override
    public void handleBlockBreakAction(
        BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence)
    {
        super.handleBlockBreakAction(pos, action, direction, maxY, sequence);
        ViveMain.MC_MODS.gameModeHelper().postHandleBlockBreakAction(this);
    }
}
