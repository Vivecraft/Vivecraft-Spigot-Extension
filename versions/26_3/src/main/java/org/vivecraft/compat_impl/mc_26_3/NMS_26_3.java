package org.vivecraft.compat_impl.mc_26_3;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.vivecraft.compat_impl.mc_26_2.NMS_26_2;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.function.Function;

public class NMS_26_3 extends NMS_26_2 {

    @Override
    protected void initShield() {
        this.LivingEntity_blockedByItem = ReflectionMethod.getRaw(LivingEntity.class, "blockedByItem", true,
            LivingEntity.class, DamageSource.class, float.class, boolean.class);
        this.BlocksAttacks_disablePaper = ReflectionMethod.getRaw(BlocksAttacks.class, "disable", false,
            ServerLevel.class, LivingEntity.class, float.class, ItemStack.class, LivingEntity.class);
    }

    @Override
    protected void doAttackerKnockback(
        LivingEntity attacker, LivingEntity player, DamageSource damageSource, float damage, boolean fullyBlocked)
    {
        this.LivingEntity_blockedByItem.invoke(attacker, player, damageSource, damage, fullyBlocked);
    }

    @Override
    protected boolean isVREnderMan(Object entity) {
        return VREnderMan.isVREnderMan(entity);
    }

    @Override
    protected Function<Entity, Entity> newVREnderman() {
        return VREnderMan.VREnderManSupplier();
    }
}
