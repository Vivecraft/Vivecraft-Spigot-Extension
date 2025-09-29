package org.vivecraft.compat_impl.mc_1_11;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.ItemStackMapping;
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
        if (hand == VRBodyPart.MAIN_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            ((List) this.Inventory_items.get(inventory)).set((int) this.Inventory_selected.get(inventory), itemStack);
        } else if (hand == VRBodyPart.OFF_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            ((List) this.Inventory_offhandSlot.get(inventory)).set(0, itemStack);
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
}
