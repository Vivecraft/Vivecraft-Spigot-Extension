package org.vivecraft.compat_impl.mc_1_11;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.accessors.PlayerMapping;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_9.NMS_1_9;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;

public class NMS_1_11 extends NMS_1_9 {

    protected ReflectionMethod Entity_getEyePosition;
    protected ReflectionField ItemStack_EMPTY;
    protected ReflectionMethod ItemStack_isEmpty;

    private ReflectionMethod Player_disableShield;

    @Override
    protected void init() {
        super.init();
        this.Entity_getEyePosition = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_EYE_POSITION);
        this.ItemStack_isEmpty = ReflectionMethod.getMethod(ItemStackMapping.METHOD_IS_EMPTY);
    }

    @Override
    protected void initInventory() {
        super.initInventory();
        this.ItemStack_EMPTY = ReflectionField.getField(ItemStackMapping.FIELD_EMPTY);
    }

    @Override
    protected void initShield() {
        initShieldDisable();
        initShieldAttackKnockback();
    }

    protected void initShieldDisable() {
        this.Player_disableShield = ReflectionMethod.getMethod(true, PlayerMapping.METHOD_DISABLE_SHIELD_2);
    }

    protected void initShieldAttackKnockback() {}

    @Override
    protected Vector getEyePosition(Object nmsEntity) {
        return vec3ToVector(this.Entity_getEyePosition.invoke(nmsEntity, 1F));
    }

    @Override
    public Object getHandItemInternal(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.MAIN_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            return ((List) this.Inventory_items.get(inventory)).get((int) this.Inventory_selected.get(inventory));
        } else if (hand == VRBodyPart.OFF_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            return ((List) this.Inventory_offhandSlot.get(inventory)).get(0);
        }
        return this.ItemStack_EMPTY.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setHandItemInternal(Player player, VRBodyPart hand, Object itemStack) {
        if (hand.isHand() && getHandItemInternal(player, hand) != itemStack) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            if (hand == VRBodyPart.MAIN_HAND) {
                int selected = (int) this.Inventory_selected.get(inventory);
                int tempSelection = selected == 0 ? 1 : 0;
                // temporarily change the selected item index, to not trigger any item switching hooks
                this.Inventory_selected.set(inventory, tempSelection);
                ((List) this.Inventory_items.get(inventory)).set(selected, itemStack);
                this.Inventory_selected.set(inventory, selected);
            } else {
                ((List) this.Inventory_offhandSlot.get(inventory)).set(0, itemStack);
            }
        }
    }

    @Override
    public void applyEquipmentChange(Player player, Object oldItemStack, Object newItemStack) {
        if (!(boolean) this.ItemStack_matches.invokes(oldItemStack, newItemStack)) {
            Object attributes = this.LivingEntity_getAttributes.invoke(BukkitReflector.getEntityHandle(player));
            if (!(boolean) this.ItemStack_isEmpty.invoke(oldItemStack)) {
                this.AttributeMap_removeAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(oldItemStack, this.EquipmentSlot_MAINHAND.get()));
            }
            if (!(boolean) this.ItemStack_isEmpty.invoke(newItemStack)) {
                this.AttributeMap_addAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(newItemStack, this.EquipmentSlot_MAINHAND.get()));
            }
        } else {
            Debug.log("stacks match, don't touch attributes");
        }
    }

    @Override
    public float doShieldBlocking(
        Player player, ItemStack itemStack, VRBodyPart hand, double angle, Entity attacker, float damage,
        EntityDamageEvent event)
    {
        super.doShieldBlocking(player, itemStack, hand, angle, attacker, damage, event);
        // all damage is blocked in this version
        return damage;
    }

    @Override
    protected void disableShield(Player player, LivingEntity attacker, ItemStack itemStack) {
        if (canDisableShield(attacker)) {
            this.Player_disableShield.invoke(BukkitReflector.getEntityHandle(player), true);
        }
    }

    @Override
    protected void shieldAttackerKnockback(Player player, LivingEntity attacker) {
        // no knockback in this version, since all damage is always blocked
    }
}
