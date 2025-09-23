package org.vivecraft.compat_impl.mc_X_X;

import org.vivecraft.ViveMain;

public class VREndermanFreezeWhenLookAt extends net.minecraft.world.entity.ai.goal.Goal {

    private final net.minecraft.world.entity.monster.EnderMan enderman;

    public VREndermanFreezeWhenLookAt(net.minecraft.world.entity.monster.EnderMan enderman) {
        this.enderman = enderman;
        ViveMain.MC_MODS.endermanHelper().freezeSetFlags(this);
    }

    @Override
    public boolean canUse() {
        return ViveMain.MC_MODS.endermanHelper().freezeCanUse(this.enderman);
    }

    @Override
    public void start() {
        ViveMain.MC_MODS.endermanHelper().freezeStart(this.enderman);
    }

    @Override
    public void tick() {
        ViveMain.MC_MODS.endermanHelper().freezeTick(this.enderman);
    }
}
