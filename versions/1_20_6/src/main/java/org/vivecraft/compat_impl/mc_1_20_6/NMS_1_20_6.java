package org.vivecraft.compat_impl.mc_1_20_6;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_20_2.NMS_1_20_2;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class NMS_1_20_6 extends NMS_1_20_2 {

    // custom item name
    protected ReflectionMethod ItemStack_set;
    protected ReflectionField DataComponents_CUSTOM_NAME;

    // armor
    protected ReflectionField DataComponents_ATTRIBUTE_MODIFIERS;
    protected ReflectionMethod DataComponent_getOrDefault;
    protected ReflectionField ItemAttributeModifiers_EMPTY;
    protected ReflectionMethod ItemAttributeModifiers_modifiers;
    protected ReflectionMethod ItemAttributeModifiersEntry_attribute;
    protected ReflectionMethod ItemAttributeModifiersEntry_modifier;
    protected ReflectionMethod Holder_is;

    protected ReflectionMethod ItemStack_forEachModifier;
    protected ReflectionMethod AttributeMap_getInstance;
    protected ReflectionMethod AttributeInstance_addTransientModifier;
    protected ReflectionMethod AttributeInstance_removeModifier;

    @Override
    protected void init() {
        super.init();
        this.ItemStack_set = ReflectionMethod.getMethod(ItemStackMapping.METHOD_SET);
        this.DataComponents_CUSTOM_NAME = ReflectionField.getField(DataComponentsMapping.FIELD_CUSTOM_NAME);
    }

    @Override
    protected void initArmor() {
        super.initArmor();
        this.DataComponent_getOrDefault = ReflectionMethod.getMethod(DataComponentHolderMapping.METHOD_GET_OR_DEFAULT);
        this.DataComponents_ATTRIBUTE_MODIFIERS = ReflectionField.getField(
            DataComponentsMapping.FIELD_ATTRIBUTE_MODIFIERS);
        this.ItemAttributeModifiers_EMPTY = ReflectionField.getField(ItemAttributeModifiersMapping.FIELD_EMPTY);
        this.ItemAttributeModifiers_modifiers = ReflectionMethod.getMethod(
            ItemAttributeModifiersMapping.METHOD_MODIFIERS);
        this.ItemAttributeModifiersEntry_attribute = ReflectionMethod.getMethod(
            ItemAttributeModifiers$EntryMapping.METHOD_ATTRIBUTE);
        this.ItemAttributeModifiersEntry_modifier = ReflectionMethod.getMethod(
            ItemAttributeModifiers$EntryMapping.METHOD_MODIFIER);
        this.Holder_is = ReflectionMethod.getMethod(HolderMapping.METHOD_IS);
    }

    @Override
    protected void initDualWielding() {
        this.ItemStack_forEachModifier = ReflectionMethod.getMethod(ItemStackMapping.METHOD_FOR_EACH_MODIFIER);
        this.AttributeInstance_addTransientModifier = ReflectionMethod.getMethod(
            AttributeInstanceMapping.METHOD_ADD_TRANSIENT_MODIFIER);
        this.AttributeInstance_removeModifier = ReflectionMethod.getMethod(
            AttributeInstanceMapping.METHOD_REMOVE_MODIFIER);
        this.AttributeMap_getInstance = ReflectionMethod.getMethod(AttributeMapMapping.METHOD_GET_INSTANCE);
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = BukkitReflector.asNMSCopy(itemStack);
        this.ItemStack_set.invoke(nmsStack, this.DataComponents_CUSTOM_NAME.get(),
            this.Component_translationWithFallback.invokes(translationKey, fallback));
        return BukkitReflector.asBukkitCopy(nmsStack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public double getArmorValue(ItemStack itemStack) {
        Object stack = BukkitReflector.asNMSCopy(itemStack);
        List<Object> modifiers = (List) this.ItemAttributeModifiers_modifiers.invoke(
            this.DataComponent_getOrDefault.invoke(stack, this.DataComponents_ATTRIBUTE_MODIFIERS.get(),
                this.ItemAttributeModifiers_EMPTY.get()));
        if (modifiers.isEmpty() && this.Item_getDefaultAttributeModifiers != null) {
            // use the defaults
            modifiers = (List) this.ItemAttributeModifiers_modifiers.invoke(
                this.Item_getDefaultAttributeModifiers.invoke(this.ItemStack_getItem.invoke(stack)));
        }

        modifiers = modifiers.stream().filter(
                entry -> (boolean) this.Holder_is.invoke(this.ItemAttributeModifiersEntry_attribute.invoke(entry),
                    this.Attributes_ARMOR.get())).map(o -> this.ItemAttributeModifiersEntry_modifier.invoke(o))
            .collect(Collectors.toList());

        return applyAttributeModifiers(0, modifiers);
    }

    @Override
    protected void placeDataItem(Object dataItem, int id, Object entityData) {
        Object[] idMap = (Object[]) this.SynchedEntityData_itemsById.get(entityData);
        if (id < idMap.length) {
            idMap[id] = dataItem;
        } else {
            Debug.log("Data pose index is higher than data array size???");
        }
    }

    @Override
    public void applyEquipmentChange(Player player, Object oldItemStack, Object newItemStack) {
        if (!(boolean) this.ItemStack_matches.invokes(oldItemStack, newItemStack)) {
            Object attributes = this.LivingEntity_getAttributes.invoke(BukkitReflector.getEntityHandle(player));
            if (!(boolean) this.ItemStack_isEmpty.invoke(oldItemStack)) {
                this.ItemStack_forEachModifier.invoke(oldItemStack, this.EquipmentSlot_MAINHAND.get(),
                    (BiConsumer) (holder, modifier) -> {
                        Object instance = this.AttributeMap_getInstance.invoke(attributes, holder);
                        if (modifier != null) {
                            this.AttributeInstance_removeModifier.invoke(instance, modifier);
                        }
                    });
            }
            if (!(boolean) this.ItemStack_isEmpty.invoke(newItemStack)) {
                this.ItemStack_forEachModifier.invoke(newItemStack, this.EquipmentSlot_MAINHAND.get(),
                    (BiConsumer) (holder, modifier) -> {
                        Object instance = this.AttributeMap_getInstance.invoke(attributes, holder);
                        if (modifier != null) {
                            this.AttributeInstance_removeModifier.invoke(instance, modifier);
                            this.AttributeInstance_addTransientModifier.invoke(instance, modifier);
                        }
                    });
            }
        }
    }
}
