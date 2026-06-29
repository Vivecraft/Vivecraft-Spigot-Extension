package org.vivecraft.compat_impl.mc_1_21_5;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_21_3.NMS_1_21_3;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Optional;

public class NMS_1_21_5 extends NMS_1_21_3 {

    protected ReflectionField Inventory_equipment;
    protected ReflectionMethod EntityEquipment_get;
    protected ReflectionMethod EntityEquipment_set;
    protected ReflectionField EquipmentSlot_OFFHAND;

    protected ReflectionMethod DataComponentHolder_get;
    protected ReflectionField DataComponents_BLOCKS_ATTACKS;
    protected ReflectionMethod BlocksAttacks_onBlocked;
    protected ReflectionMethod BlocksAttacks_bypassedBy;
    protected ReflectionMethod BlocksAttacks_resolveBlockedDamage;
    protected ReflectionMethod BlocksAttacks_hurtBlockingItem;
    protected ReflectionMethod BlocksAttacks_disable;
    protected ReflectionMethod BlocksAttacks_disablePaper;
    protected ReflectionField BlocksAttacks_disableSound;
    protected ReflectionMethod DamageSource_getDirectEntity;
    protected ReflectionMethod DamageSource_getSourcePosition;
    protected ReflectionField DamageTypeTags_IS_PROJECTILE;
    protected ReflectionField DamageTypeTags_NO_IMPACT;
    protected ReflectionField DamageTypeTags_NO_KNOCKBACK;
    protected ReflectionMethod Level_playSound;
    protected ReflectionMethod Entity_getSoundSource;

    protected ReflectionField InteractionHand_MAIN_HAND;
    protected ReflectionField InteractionHand_OFF_HAND;

    protected ReflectionMethod LivingEntity_getSecondsToDisableBlocking;
    protected ReflectionMethod LivingEntity_knockback;
    protected ReflectionField Entity_hurtMarked;
    protected Class<?> Projectile;
    protected ReflectionMethod Projectile_calculateHorizontalHurtKnockbackDirection;

    protected Class<?> LivingEntity;

    @Override
    protected void init() {
        super.init();
        this.DataComponentHolder_get = ReflectionMethod.getMethod(DataComponentHolderMapping.METHOD_GET);
    }

    @Override
    protected void initInventory() {
        this.Player_inventory = ReflectionField.getField(PlayerMapping.FIELD_INVENTORY);
        this.Inventory_items = ReflectionField.getField(InventoryMapping.FIELD_ITEMS);
        this.Inventory_selected = ReflectionField.getField(InventoryMapping.FIELD_SELECTED);
        this.ItemStack_EMPTY = ReflectionField.getField(ItemStackMapping.FIELD_EMPTY);
        this.Inventory_equipment = ReflectionField.getField(InventoryMapping.FIELD_EQUIPMENT);
        this.EntityEquipment_get = ReflectionMethod.getMethod(EntityEquipmentMapping.METHOD_GET);
        this.EntityEquipment_set = ReflectionMethod.getMethod(EntityEquipmentMapping.METHOD_SET);
        this.EquipmentSlot_OFFHAND = ReflectionField.getField(EquipmentSlotMapping.FIELD_OFFHAND);
    }

    @Override
    protected void initShield() {
        super.initShield();
        this.DataComponents_BLOCKS_ATTACKS = ReflectionField.getField(DataComponentsMapping.FIELD_BLOCKS_ATTACKS);
        this.BlocksAttacks_onBlocked = ReflectionMethod.getMethod(BlocksAttacksMapping.METHOD_ON_BLOCKED);
        this.BlocksAttacks_bypassedBy = ReflectionMethod.getMethod(BlocksAttacksMapping.METHOD_BYPASSED_BY);
        this.BlocksAttacks_resolveBlockedDamage = ReflectionMethod.getMethod(
            BlocksAttacksMapping.METHOD_RESOLVE_BLOCKED_DAMAGE);
        this.BlocksAttacks_hurtBlockingItem = ReflectionMethod.getMethod(
            BlocksAttacksMapping.METHOD_HURT_BLOCKING_ITEM);
        this.BlocksAttacks_disableSound = ReflectionField.getField(BlocksAttacksMapping.FIELD_DISABLE_SOUND);
        this.Level_playSound = ReflectionMethod.getMethod(LevelMapping.METHOD_PLAY_SOUND);
        this.Entity_getSoundSource = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_SOUND_SOURCE);

        this.DamageSource_getDirectEntity = ReflectionMethod.getMethod(DamageSourceMapping.METHOD_GET_DIRECT_ENTITY);
        this.DamageSource_getSourcePosition = ReflectionMethod.getMethod(
            DamageSourceMapping.METHOD_GET_SOURCE_POSITION);
        this.DamageTypeTags_IS_PROJECTILE = ReflectionField.getField(DamageTypeTagsMapping.FIELD_IS_PROJECTILE);
        this.DamageTypeTags_NO_IMPACT = ReflectionField.getField(DamageTypeTagsMapping.FIELD_NO_IMPACT);
        this.DamageTypeTags_NO_KNOCKBACK = ReflectionField.getField(DamageTypeTagsMapping.FIELD_NO_KNOCKBACK);

        this.InteractionHand_MAIN_HAND = ReflectionField.getField(InteractionHandMapping.FIELD_MAIN_HAND);
        this.InteractionHand_OFF_HAND = ReflectionField.getField(InteractionHandMapping.FIELD_OFF_HAND);

        this.LivingEntity = ClassGetter.getClass(true, LivingEntityMapping.MAPPING);
        this.LivingEntity_knockback = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_KNOCKBACK_1);

        this.Entity_hurtMarked = ReflectionField.getField(EntityMapping.FIELD_HURT_MARKED);

        this.Projectile = ClassGetter.getClass(true, ProjectileMapping.MAPPING);
        this.Projectile_calculateHorizontalHurtKnockbackDirection = ReflectionMethod.getMethod(
            ProjectileMapping.METHOD_CALCULATE_HORIZONTAL_HURT_KNOCKBACK_DIRECTION);
    }

    @Override
    protected void initShieldDisable() {
        this.LivingEntity_getSecondsToDisableBlocking = ReflectionMethod.getMethod(
            LivingEntityMapping.METHOD_GET_SECONDS_TO_DISABLE_BLOCKING);
        this.BlocksAttacks_disable = ReflectionMethod.getMethod(false, BlocksAttacksMapping.METHOD_DISABLE);
        this.BlocksAttacks_disablePaper = ReflectionMethod.getRaw(
            ClassGetter.getClass(true, BlocksAttacksMapping.MAPPING), "disable", false,
            ClassGetter.getClass(true, ServerLevelMapping.MAPPING),
            ClassGetter.getClass(true, LivingEntityMapping.MAPPING),
            float.class,
            ClassGetter.getClass(true, ItemStackMapping.MAPPING),
            ClassGetter.getClass(true, LivingEntityMapping.MAPPING));

        if (this.BlocksAttacks_disable == null && this.BlocksAttacks_disablePaper == null) {
            throw new RuntimeException("BlocksAttacks_disable and BlocksAttacks_disablePaper is null");
        }
    }

    @Override
    public Object getHandItemInternal(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.OFF_HAND) {
            Object equipment = this.Inventory_equipment.get(
                this.Player_inventory.get(BukkitReflector.getEntityHandle(player)));
            return this.EntityEquipment_get.invoke(equipment, this.EquipmentSlot_OFFHAND.get());
        }
        return super.getHandItemInternal(player, hand);
    }

    @Override
    public void setHandItemInternal(Player player, VRBodyPart hand, Object itemStack) {
        if (hand.isHand()) {
            Object slot =
                hand == VRBodyPart.MAIN_HAND ? this.EquipmentSlot_MAINHAND.get() : this.EquipmentSlot_OFFHAND.get();
            Object equipment = this.Inventory_equipment.get(
                this.Player_inventory.get(BukkitReflector.getEntityHandle(player)));
            this.EntityEquipment_set.invoke(equipment, slot, itemStack);
        }
    }

    @Override
    public boolean isShield(ItemStack itemStack) {
        // spigot technically has an api call to get the "BlocksAttacks" component, but paper doesn't support it :(
        return this.DataComponentHolder_get.invoke(BukkitReflector.asNMSCopy(itemStack),
            this.DataComponents_BLOCKS_ATTACKS.get()) != null;
    }

    @Override
    public boolean doesBlockDamage(ItemStack itemStack, EntityDamageEvent damage) {
        Object blocksAttacks = this.DataComponentHolder_get.invoke(BukkitReflector.asNMSCopy(itemStack),
            this.DataComponents_BLOCKS_ATTACKS.get());
        if (blocksAttacks != null) {
            Optional<?> bypass = (Optional<?>) this.BlocksAttacks_bypassedBy.invoke(blocksAttacks);
            return !bypass.isPresent() ||
                !(boolean) this.DamageSource_is.invoke(BukkitReflector.getDamageSourceHandle(damage.getDamageSource()),
                    bypass.get());
        }
        // not a blocking item
        return false;
    }

    @Override
    public float doShieldBlocking(
        Player player, ItemStack itemStack, VRBodyPart hand, double angle, Entity attacker, float damage,
        EntityDamageEvent event)
    {
        // get the actual backing itemstack, since we are changing it and not just reading it
        Object nmsStack = BukkitReflector.getItemHandle(itemStack);
        if (nmsStack != null) {
            Object blocksAttacks = this.DataComponentHolder_get.invoke(nmsStack,
                this.DataComponents_BLOCKS_ATTACKS.get());
            if (blocksAttacks != null) {
                Object damageSource = BukkitReflector.getDamageSourceHandle(event.getDamageSource());
                if (damageSource != null) {
                    Object nmsAttacker = BukkitReflector.getEntityHandle(attacker);
                    Object nmsPlayer = BukkitReflector.getEntityHandle(player);

                    // reduce damage
                    float damageBlocked = (float) this.BlocksAttacks_resolveBlockedDamage.invoke(blocksAttacks,
                        damageSource, damage, angle);
                    boolean blocked = damageBlocked > 0;

                    Object level = this.Entity_getLevel.invoke(nmsPlayer);
                    // damage item
                    this.BlocksAttacks_hurtBlockingItem.invoke(blocksAttacks, level, nmsStack, nmsPlayer,
                        hand == VRBodyPart.MAIN_HAND ? this.InteractionHand_MAIN_HAND.get() :
                            this.InteractionHand_OFF_HAND.get(),
                        damageBlocked);

                    if (blocked &&
                        !(boolean) this.DamageSource_is.invoke(damageSource, this.DamageTypeTags_IS_PROJECTILE.get()) &&
                        this.LivingEntity.isInstance(nmsAttacker))
                    {
                        // attacker knockback
                        this.LivingEntity_blockedByItem.invoke(nmsAttacker, nmsPlayer);

                        // do disable
                        float secondsToDisableBlocking = (float) this.LivingEntity_getSecondsToDisableBlocking.invoke(
                            nmsAttacker);
                        if (secondsToDisableBlocking > 0.0F) {
                            if (this.BlocksAttacks_disablePaper != null) {
                                this.BlocksAttacks_disablePaper.invoke(blocksAttacks, level, nmsPlayer,
                                    secondsToDisableBlocking, nmsStack, nmsAttacker);
                            } else {
                                this.BlocksAttacks_disable.invoke(blocksAttacks, level, nmsPlayer,
                                    secondsToDisableBlocking, nmsStack);
                            }
                        }
                    }

                    // play blocking sound
                    if (blocked) {
                        this.BlocksAttacks_onBlocked.invoke(blocksAttacks, level, nmsPlayer);
                    }

                    // mark hurt to send knockback to clients
                    if (!(boolean) this.DamageSource_is.invoke(damageSource, this.DamageTypeTags_NO_IMPACT.get()) &&
                        (!blocked || damage - damageBlocked > 0.0F))
                    {
                        this.Entity_hurtMarked.set(nmsPlayer, true);
                    }

                    // player knockback
                    if (!(boolean) this.DamageSource_is.invoke(damageSource, this.DamageTypeTags_NO_KNOCKBACK.get())) {
                        double xd = 0.0;
                        double zd = 0.0;
                        Object directEntity = this.DamageSource_getDirectEntity.invoke(damageSource);
                        Object damagePos = this.DamageSource_getSourcePosition.invoke(damageSource);

                        if (this.Projectile.isInstance(directEntity)) {
                            DoubleDoubleImmutablePair knockbackDirection = (DoubleDoubleImmutablePair) this.Projectile_calculateHorizontalHurtKnockbackDirection.invoke(
                                directEntity, nmsPlayer, damageSource);
                            xd = -knockbackDirection.leftDouble();
                            zd = -knockbackDirection.rightDouble();
                        } else if (damagePos != null) {
                            xd = (double) this.Vec3_X.get(damagePos) - player.getLocation().getX();
                            zd = (double) this.Vec3_Z.get(damagePos) - player.getLocation().getZ();
                        }
                        this.LivingEntity_knockback.invoke(nmsPlayer, 0.4F, xd, zd);
                    }
                    return damageBlocked;
                }
            }
        }
        return 0;
    }

    @Override
    public void playShieldBlockSound(Player player, ItemStack itemStack) {
        Object blocksAttacks = this.DataComponentHolder_get.invoke(BukkitReflector.asNMSCopy(itemStack),
            this.DataComponents_BLOCKS_ATTACKS.get());
        if (blocksAttacks != null) {
            Object entity = BukkitReflector.getEntityHandle(player);
            this.BlocksAttacks_onBlocked.invoke(blocksAttacks, this.getLevel(entity), entity);
        }
    }

    @Override
    public void playShieldDisableSound(Player player, ItemStack itemStack) {
        Object blocksAttacks = this.DataComponentHolder_get.invoke(BukkitReflector.asNMSCopy(itemStack),
            this.DataComponents_BLOCKS_ATTACKS.get());
        if (blocksAttacks != null) {
            Object nmsPlayer = BukkitReflector.getEntityHandle(player);
            Optional<?> disableSound = (Optional<?>) this.BlocksAttacks_disableSound.get(blocksAttacks);
            if (disableSound != null && disableSound.isPresent()) {
                Location location = player.getLocation();
                this.Level_playSound.invoke(this.getLevel(nmsPlayer), null, location.getX(), location.getY(),
                    location.getZ(), disableSound.get(), this.Entity_getSoundSource.invoke(nmsPlayer), 0.8F,
                    0.8F + this.random.nextFloat() * 0.4F);
            }
        }
    }
}
