package org.vivecraft.compat_impl.mc_26_2;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.vivecraft.compat_impl.mc_26_1.NMS_26_1;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_26_2 extends NMS_26_1 {

    @Override
    protected void initShield() {
        this.LivingEntity_blockedByItem = ReflectionMethod.getRaw(LivingEntity.class, "blockedByItem", true,
            LivingEntity.class, DamageSource.class, float.class);
        this.BlocksAttacks_disablePaper = ReflectionMethod.getRaw(BlocksAttacks.class, "disable", false,
            ServerLevel.class, LivingEntity.class, float.class, ItemStack.class, LivingEntity.class);
    }

    @Override
    protected void doAttackerKnockback(
        LivingEntity attacker, LivingEntity player, DamageSource damageSource, float damage)
    {
        this.LivingEntity_blockedByItem.invoke(attacker, player, damageSource, damage);
    }

    @Override
    protected void doBlockKnockback(ServerPlayer player, DamageSource damageSource, float damage) {
        player.dealDefaultKnockback(damageSource, damage, true);
    }
}
