package org.vivecraft.compat_impl.mc_1_21_5;

import org.bukkit.entity.Player;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_21_3.NMS_1_21_3;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_21_5 extends NMS_1_21_3 {

    protected ReflectionField Inventory_equipment;
    protected ReflectionMethod EntityEquipment_get;
    protected ReflectionMethod EntityEquipment_set;
    protected ReflectionField EquipmentSlot_OFFHAND;

    @Override
    protected void initInventory() {
        this.Player_inventory = ReflectionField.getField(PlayerMapping.FIELD_INVENTORY);
        this.Inventory_items = ReflectionField.getField(InventoryMapping.FIELD_ITEMS);
        this.Inventory_selected = ReflectionField.getField(InventoryMapping.FIELD_SELECTED);
        this.ItemStack_EMPTY = ReflectionField.getField(ItemStackMapping.FIELD_EMPTY);
        this.Inventory_equipment = ReflectionField.getField(InventoryMapping.FIELD_EQUIPMENT);
        this.EntityEquipment_get = ReflectionMethod.getMethod(EntityEquipmentMapping.METHOD_GET);
        this.EquipmentSlot_OFFHAND = ReflectionField.getField(EquipmentSlotMapping.FIELD_OFFHAND);
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
        if (hand == VRBodyPart.OFF_HAND) {
            Object equipment = this.Inventory_equipment.get(
                this.Player_inventory.get(BukkitReflector.getEntityHandle(player)));
            this.EntityEquipment_set.invoke(equipment, this.EquipmentSlot_OFFHAND.get(), itemStack);
        } else {
            super.setHandItemInternal(player, hand, itemStack);
        }
    }
}
