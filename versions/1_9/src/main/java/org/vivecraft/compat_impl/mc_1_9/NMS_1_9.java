package org.vivecraft.compat_impl.mc_1_9;

import com.google.common.collect.Multimap;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_8.NMS_1_8;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Collection;
import java.util.Random;

public class NMS_1_9 extends NMS_1_8 {

    protected final Random random = new Random();

    protected ReflectionMethod ServerPlayer_getServer;

    protected ReflectionField EquipmentSlot_FEET;
    protected ReflectionField EquipmentSlot_MAINHAND;
    protected ReflectionField Attributes_ARMOR;
    protected ReflectionMethod Attribute_getDescriptionId;
    protected ReflectionMethod Item_getDefaultAttributeModifiers;
    protected ReflectionMethod AttributeModifier_getAmount;
    protected ReflectionMethod AttributeModifier_getOperation;

    protected ReflectionField Inventory_offhandSlot;

    protected ReflectionField ServerboundUseItemOnPacket_blockPos;
    protected ReflectionField ServerboundUseItemOnPacket_hitDir;

    @Override
    protected void init() {
        super.init();
        this.EquipmentSlot_MAINHAND = ReflectionField.getField(EquipmentSlotMapping.FIELD_MAINHAND);
    }

    @Override
    protected void initServer() {
        this.ServerPlayer_getServer = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_SERVER);
    }

    @Override
    protected void initArmor() {
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
        this.Item_getDefaultAttributeModifiers = ReflectionMethod.getMethod(false,
            ItemMapping.METHOD_GET_DEFAULT_ATTRIBUTE_MODIFIERS_1,
            ItemMapping.METHOD_GET_DEFAULT_ATTRIBUTE_MODIFIERS,
            ItemMapping.METHOD_FUNC_111205_H);
        this.EquipmentSlot_FEET = ReflectionField.getField(EquipmentSlotMapping.FIELD_FEET);
        this.initAttributes();
    }

    protected void initAttributes() {
        this.Attributes_ARMOR = ReflectionField.getField(
            AttributesMapping.FIELD_ARMOR_1, AttributesMapping.FIELD_ARMOR);
        this.Attribute_getDescriptionId = ReflectionMethod.getMethod(AttributeMapping.METHOD_GET_DESCRIPTION_ID);
        this.AttributeModifier_getAmount = ReflectionMethod.getMethod(AttributeModifierMapping.METHOD_AMOUNT,
            AttributeModifierMapping.METHOD_GET_AMOUNT);
        this.AttributeModifier_getOperation = ReflectionMethod.getMethod(AttributeModifierMapping.METHOD_OPERATION,
            AttributeModifierMapping.METHOD_GET_OPERATION, AttributeModifierMapping.METHOD_FUNC_111169_C);
    }

    @Override
    protected void initInventory() {
        super.initInventory();
        this.Inventory_offhandSlot = ReflectionField.getField(InventoryMapping.FIELD_OFFHAND,
            InventoryMapping.FIELD_EXTRA_SLOTS);
    }

    @Override
    protected void initUseItemOnPacketAccess() {
        this.ServerboundUseItemOnPacket_blockPos = ReflectionField.getField(
            ServerboundUseItemOnPacketMapping.FIELD_FIELD_179725_B);
        this.ServerboundUseItemOnPacket_hitDir = ReflectionField.getField(
            ServerboundUseItemOnPacketMapping.FIELD_FIELD_149579_D);
    }

    @Override
    public Object getServer(Object serverPlayer) {
        return this.ServerPlayer_getServer.invoke(serverPlayer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public double getArmorValue(ItemStack itemStack) {
        Object item = this.ItemStack_getItem.invoke(BukkitReflector.asNMSCopy(itemStack));
        Multimap map = (Multimap) this.Item_getDefaultAttributeModifiers.invoke(item, this.EquipmentSlot_FEET.get());
        return applyAttributeModifiers(0F,
            map.get(this.Attribute_getDescriptionId.invoke(this.Attributes_ARMOR.get())));
    }

    protected double applyAttributeModifiers(double original, Collection<Object> modifiers) {
        for (Object modifier : modifiers) {
            double amount = (double) this.AttributeModifier_getAmount.invoke(modifier);
            int operation = (int) this.AttributeModifier_getOperation.invoke(modifier);
            switch (operation) {
                case 0:
                    original += amount;
                    break;
                case 1:
                    original += amount * original;
                    break;
                case 2:
                    original *= 1 + amount;
                    break;
            }
        }
        return original;
    }

    @Override
    public Object getHandItemInternal(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.OFF_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            return ((Object[]) this.Inventory_offhandSlot.get(inventory))[0];
        } else {
            return super.getHandItemInternal(player, hand);
        }
    }

    @Override
    public void setHandItemInternal(Player player, VRBodyPart hand, Object itemStack) {
        if (hand == VRBodyPart.OFF_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            ((Object[]) this.Inventory_offhandSlot.get(inventory))[0] = itemStack;
        } else if (hand == VRBodyPart.MAIN_HAND) {
            super.setHandItemInternal(player, hand, itemStack);
        }
    }

    @Override
    public void applyEquipmentChange(Player player, Object oldItemStack, Object newItemStack) {
        if (!(boolean) this.ItemStack_matches.invokes(oldItemStack, newItemStack)) {
            Object attributes = this.LivingEntity_getAttributes.invoke(BukkitReflector.getEntityHandle(player));
            if (oldItemStack != null) {
                this.AttributeMap_removeAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(oldItemStack, this.EquipmentSlot_MAINHAND.get()));
            }
            if (newItemStack != null) {
                this.AttributeMap_addAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(newItemStack, this.EquipmentSlot_MAINHAND.get()));
            }
        }
    }

    @Override
    protected boolean isInteractPacket(Object packet) {
        return this.ServerboundUseItemOnPacket.isInstance(packet);
    }

    @Override
    protected Object getUseItemOnDir(Object packet) {
        return this.ServerboundUseItemOnPacket_hitDir.get(packet);
    }

    @Override
    protected Object getUseItemOnPos(Object packet) {
        return this.ServerboundUseItemOnPacket_blockPos.get(packet);
    }

    @Override
    public void playShieldBlockSound(Player player, ItemStack itemStack) {
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
    }
}
