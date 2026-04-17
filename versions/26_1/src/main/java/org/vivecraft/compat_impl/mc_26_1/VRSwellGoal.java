package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.vivecraft.ViveMain;

public class VRSwellGoal extends SwellGoal {

    private final Creeper creeper;

    public VRSwellGoal(Creeper creeper) {
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
