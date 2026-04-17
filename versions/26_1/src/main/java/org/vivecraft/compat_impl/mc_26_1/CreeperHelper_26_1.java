package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.entities.CreeperHelper;
import org.vivecraft.util.reflection.ReflectionField;

public class CreeperHelper_26_1 implements CreeperHelper {

    protected ReflectionField SwellGoal_target;

    public CreeperHelper_26_1() {
        this.init();
    }

    protected void init() {
        this.SwellGoal_target = ReflectionField.getRaw(SwellGoal.class, "target");
    }

    @Override
    public boolean isSwellGoal(Object goal) {
        return goal instanceof SwellGoal;
    }

    @Override
    public Object getCreeperSwellGoal(Object creeper) {
        return new VRSwellGoal((Creeper) creeper);
    }

    @Override
    public boolean creeperVrSwellCheck(Object creeper) {
        Creeper nmsCreeper = (Creeper) creeper;
        LivingEntity target = nmsCreeper.getTarget();

        return nmsCreeper.getSwellDir() > 0 || (target != null && ((Creeper) creeper).distanceToSqr(target) <
            ViveMain.CONFIG.creeperSwellDistance.get() * ViveMain.CONFIG.creeperSwellDistance.get()
        );
    }

    @Override
    public void creeperVrSwellTick(Object creeper, Object swellGoal) {
        Creeper nmsCreeper = (Creeper) creeper;
        LivingEntity target = (LivingEntity) this.SwellGoal_target.get(swellGoal);
        double distance = ViveMain.CONFIG.creeperSwellDistance.get() + 4;
        if (target == null ||
            nmsCreeper.distanceToSqr(target) > distance * distance ||
            !nmsCreeper.getSensing().hasLineOfSight(target))
        {
            nmsCreeper.setSwellDir(-1);
        } else {
            nmsCreeper.setSwellDir(1);
        }
    }
}
