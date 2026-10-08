package org.vivecraft.compat_impl.mc_X_X;

import org.vivecraft.ViveMain;

public class VRDemoMode extends net.minecraft.server.level.DemoMode {

    public VRDemoMode(
        net.minecraft.server.level.ServerPlayerGameMode oldGameMode, net.minecraft.server.level.ServerPlayer player)
    {
//#1.8-1.13.2#        super(oldGameMode.world);
//#1.14-1.16.5#        super(oldGameMode.level);
//#1.17-1.99.99#        super(player);

//#1.8-1.16.5#        this.player = oldGameMode.player;

//#1.8-1.9.4#        this.func_73076_a(oldGameMode.func_73081_b());
//#1.10-1.15.2#        this.setGameModeForPlayer_1(oldGameMode.getGameModeForPlayer());
//#1.16.1-1.99.99#        this.setGameModeForPlayer(oldGameMode.getGameModeForPlayer()
//#1.16.1-1.99.99#          ,oldGameMode.getPreviousGameModeForPlayer()
//#1.16.1-1.99.99#        );
    }

    @Override
    public void tick() {
        ViveMain.MC_MODS.gameModeHelper().wrapTick(this, super::tick);
    }

//#1.14.4-1.99.99#    @Override
//#1.14.4-1.18.2#    public void handleBlockBreakAction_1(
//#1.19-1.99.99#    public void handleBlockBreakAction(
//#1.14.4-1.99.99#        net.minecraft.core.BlockPos pos,
//#1.14.4-1.99.99#        net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action action,
//#1.14.4-1.99.99#        net.minecraft.core.Direction direction, int maxY
//#1.19-1.99.99#          , int sequence
//#1.14.4-1.99.99#    )
//#1.14.4-1.99.99#    {
//#1.14.4-1.18.2#        super.handleBlockBreakAction_1(pos, action, direction, maxY);
//#1.19-1.99.99#        super.handleBlockBreakAction(pos, action, direction, maxY, sequence);
//#1.14.4-1.99.99#        ViveMain.MC_MODS.gameModeHelper().postHandleBlockBreakAction(this);
//#1.14.4-1.99.99#    }

//#1.8-1.14.3#    @Override
//#1.8-1.14.3#    public void func_180784_a(net.minecraft.core.BlockPos blockPos, net.minecraft.core.Direction direction) {
//#1.8-1.14.3#        super.func_180784_a(blockPos, direction);
//#1.8-1.14.3#        ViveMain.MC_MODS.gameModeHelper().postHandleBlockBreakAction(this);
//#1.8-1.14.3#    }
//#1.8-1.14.3#
//#1.8-1.14.3#    @Override
//#1.8-1.14.3#    public void func_180785_a(net.minecraft.core.BlockPos blockPos) {
//#1.8-1.14.3#        super.func_180785_a(blockPos);
//#1.8-1.14.3#        ViveMain.MC_MODS.gameModeHelper().postHandleBlockBreakAction(this);
//#1.8-1.14.3#    }
//#1.8-1.14.3#
//#1.8-1.14.3#    @Override
//#1.8-1.14.3#    public void func_180238_e() {
//#1.8-1.14.3#        super.func_180238_e();
//#1.8-1.14.3#        ViveMain.MC_MODS.gameModeHelper().postHandleBlockBreakAction(this);
//#1.8-1.14.3#    }
}
