package org.vivecraft.compat_impl.mc_1_9;

import com.google.common.collect.Multimap;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_8.NMS_1_8;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Collection;

public class NMS_1_9 extends NMS_1_8 {
    protected ReflectionMethod ServerPlayer_getServer;

    protected ReflectionField EquipmentSlot_FEET;
    protected ReflectionField Attributes_ARMOR;
    protected ReflectionMethod Attribute_getDescriptionId;
    protected ReflectionMethod Item_getDefaultAttributeModifiers;
    protected ReflectionMethod AttributeModifier_getAmount;
    protected ReflectionMethod AttributeModifier_getOperation;

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
}
