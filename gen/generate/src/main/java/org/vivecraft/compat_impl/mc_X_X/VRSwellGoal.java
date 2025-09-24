package org.vivecraft.compat_impl.mc_X_X;

import org.vivecraft.ViveMain;

public class VRSwellGoal extends net.minecraft.world.entity.ai.goal.SwellGoal {

    private final net.minecraft.world.entity.monster.Creeper creeper;

    public VRSwellGoal(net.minecraft.world.entity.monster.Creeper creeper) {
        super(creeper);
        this.creeper = creeper;
    }

    @Override
    public boolean canUse() {
        if (ViveMain.NMS.isTargetVrPlayer(this.creeper)) {
            return ViveMain.MC_MODS.creeperHelper().creeperVrSwellCheck(this.creeper);
        }
        return super.canUse();
    }

    @Override
    public void tick() {
        ViveMain.MC_MODS.creeperHelper().creeperVrSwellTick(this.creeper, this);
    }
}
