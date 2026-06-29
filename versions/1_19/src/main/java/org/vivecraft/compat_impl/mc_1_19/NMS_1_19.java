package org.vivecraft.compat_impl.mc_1_19;

import org.bukkit.entity.LivingEntity;
import org.vivecraft.accessors.ComponentMapping;
import org.vivecraft.accessors.LivingEntityMapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_18.NMS_1_18;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_19 extends NMS_1_18 {

    protected ReflectionMethod Component_getContents;

    private ReflectionMethod LivingEntity_canDisableShield;

    @Override
    protected void init() {
        // still need the super ones
        super.init();
        this.Component_getContents = ReflectionMethod.getMethod(ComponentMapping.METHOD_GET_CONTENTS);
    }

    @Override
    protected void initShieldDisable() {
        super.initShieldDisable();
        this.LivingEntity_canDisableShield = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_CAN_DISABLE_SHIELD);
    }

    @Override
    protected Object getTranslationComponent(Object component) {
        return this.Component_getContents.invoke(component);
    }

    @Override
    protected boolean canDisableShield(LivingEntity attacker) {
        return (boolean) this.LivingEntity_canDisableShield.invoke(BukkitReflector.getEntityHandle(attacker));
    }
}
