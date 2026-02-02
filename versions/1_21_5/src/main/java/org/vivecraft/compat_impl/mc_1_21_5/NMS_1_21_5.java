package org.vivecraft.compat_impl.mc_1_21_5;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    protected ReflectionMethod DataComponentHolder_get;
    protected ReflectionField DataComponents_BLOCKS_ATTACKS;
    protected ReflectionMethod BlocksAttack_onBlocked;

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
    protected void initShieldSound() {
        this.DataComponents_BLOCKS_ATTACKS = ReflectionField.getField(DataComponentsMapping.FIELD_BLOCKS_ATTACKS);
        this.BlocksAttack_onBlocked = ReflectionMethod.getMethod(BlocksAttacksMapping.METHOD_ON_BLOCKED);
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
    public void playShieldBlockSound(Player player, ItemStack itemStack) {
        Object blocksAttacks = this.DataComponentHolder_get.invoke(BukkitReflector.asNMSCopy(itemStack),
            this.DataComponents_BLOCKS_ATTACKS.get());
        if (blocksAttacks != null) {
            Object entity = BukkitReflector.getEntityHandle(player);
            this.BlocksAttack_onBlocked.invoke(blocksAttacks, this.getLevel(entity), entity);
        }
    }
}
