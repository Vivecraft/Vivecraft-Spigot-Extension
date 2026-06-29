package org.vivecraft.compat_impl.mc_1_20_4;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.DamageSourceMapping;
import org.vivecraft.accessors.DamageTypeTagsMapping;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_20_2.NMS_1_20_2;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_20_4 extends NMS_1_20_2 {

    protected ReflectionField DamageTypeTags_BYPASSES_SHIELD;
    protected ReflectionMethod DamageSource_is;


    @Override
    protected void initShield() {
        super.initShield();
        this.DamageSource_is = ReflectionMethod.getMethod(DamageSourceMapping.METHOD_IS);
        this.DamageTypeTags_BYPASSES_SHIELD = ReflectionField.getField(DamageTypeTagsMapping.FIELD_BYPASSES_SHIELD);
    }

    @Override
    public boolean doesBlockDamage(ItemStack itemStack, EntityDamageEvent damage) {
        Object damageSource = BukkitReflector.getDamageSourceHandle(damage.getDamageSource());
        return damageSource != null &&
            !(boolean) this.DamageSource_is.invoke(damageSource, this.DamageTypeTags_BYPASSES_SHIELD.get());
    }
}
