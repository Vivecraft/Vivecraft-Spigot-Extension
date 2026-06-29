package org.vivecraft.compat_impl.mc_1_20_6;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_20_4.NMS_1_20_4;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class NMS_1_20_6 extends NMS_1_20_4 {

    // custom item name
    protected ReflectionMethod ItemStack_set;
    protected ReflectionField DataComponents_CUSTOM_NAME;
    protected ReflectionMethod DataComponentHolder_has;

    // armor
    protected ReflectionField DataComponents_ATTRIBUTE_MODIFIERS;
    protected ReflectionMethod DataComponentHolder_getOrDefault;
    protected ReflectionField ItemAttributeModifiers_EMPTY;
    protected ReflectionMethod ItemAttributeModifiers_modifiers;
    protected ReflectionMethod ItemAttributeModifiersEntry_attribute;
    protected ReflectionMethod ItemAttributeModifiersEntry_modifier;
    protected ReflectionMethod Holder_is;

    protected ReflectionMethod ItemStack_forEachModifier;
    protected ReflectionMethod AttributeMap_getInstance;
    protected ReflectionMethod AttributeInstance_addTransientModifier;
    protected ReflectionMethod AttributeInstance_removeModifier;

    private ReflectionMethod LivingEntity_canDisableShield;
    private ReflectionMethod Player_disableShield;
    private ReflectionMethod Player_disableShield_paper;

    @Override
    protected void init() {
        super.init();
        this.ItemStack_set = ReflectionMethod.getMethod(ItemStackMapping.METHOD_SET);
        this.DataComponentHolder_has = ReflectionMethod.getMethod(DataComponentHolderMapping.METHOD_HAS);
        this.DataComponents_CUSTOM_NAME = ReflectionField.getField(DataComponentsMapping.FIELD_CUSTOM_NAME);
    }

    @Override
    protected void initArmor() {
        super.initArmor();
        this.DataComponentHolder_getOrDefault = ReflectionMethod.getMethod(
            DataComponentHolderMapping.METHOD_GET_OR_DEFAULT);
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
        this.LivingEntity_getAttributes = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_GET_ATTRIBUTES);
        this.ItemStack_forEachModifier = ReflectionMethod.getMethod(ItemStackMapping.METHOD_FOR_EACH_MODIFIER);
        this.AttributeInstance_addTransientModifier = ReflectionMethod.getMethod(
            AttributeInstanceMapping.METHOD_ADD_TRANSIENT_MODIFIER);
        this.AttributeInstance_removeModifier = ReflectionMethod.getMethod(
            AttributeInstanceMapping.METHOD_REMOVE_MODIFIER);
        this.AttributeMap_getInstance = ReflectionMethod.getMethod(AttributeMapMapping.METHOD_GET_INSTANCE);
    }

    @Override
    protected void initShieldDisable() {
        this.LivingEntity_canDisableShield = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_CAN_DISABLE_SHIELD);
        this.Player_disableShield = ReflectionMethod.getMethod(false, PlayerMapping.METHOD_DISABLE_SHIELD_1);
        this.Player_disableShield_paper = ReflectionMethod.getRaw(
            ClassGetter.getClass(true, PlayerMapping.MAPPING), "disableShield", false,
            ClassGetter.getClass(true, LivingEntityMapping.MAPPING));
        if (this.Player_disableShield == null && this.Player_disableShield_paper == null) {
            throw new RuntimeException("Player_disableShield and Player_disableShield_paper is null");
        }
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = BukkitReflector.asNMSCopy(itemStack);
        this.ItemStack_set.invoke(nmsStack, this.DataComponents_CUSTOM_NAME.get(),
            this.Component_translationWithFallback.invokes(translationKey, fallback));
        return BukkitReflector.asBukkitCopy(nmsStack);
    }

    @Override
    protected boolean hasCustomHoverName(Object nmsStack) {
        return (boolean) this.DataComponentHolder_has.invoke(nmsStack, this.DataComponents_CUSTOM_NAME.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public double getArmorValue(ItemStack itemStack) {
        Object stack = BukkitReflector.asNMSCopy(itemStack);
        List<Object> modifiers = (List) this.ItemAttributeModifiers_modifiers.invoke(
            this.DataComponentHolder_getOrDefault.invoke(stack, this.DataComponents_ATTRIBUTE_MODIFIERS.get(),
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
                        if (instance != null && modifier != null) {
                            this.AttributeInstance_removeModifier.invoke(instance, modifier);
                        }
                    });
            }
            if (!(boolean) this.ItemStack_isEmpty.invoke(newItemStack)) {
                this.ItemStack_forEachModifier.invoke(newItemStack, this.EquipmentSlot_MAINHAND.get(),
                    (BiConsumer) (holder, modifier) -> {
                        Object instance = this.AttributeMap_getInstance.invoke(attributes, holder);
                        if (instance != null && modifier != null) {
                            this.AttributeInstance_removeModifier.invoke(instance, modifier);
                            this.AttributeInstance_addTransientModifier.invoke(instance, modifier);
                        }
                    });
            }
        }
    }

    @Override
    protected void disableShield(Player player, LivingEntity attacker, ItemStack itemStack) {
        Object nmsAttacker = BukkitReflector.getEntityHandle(attacker);
        Object nmsPlayer = BukkitReflector.getEntityHandle(player);
        if ((boolean) this.LivingEntity_canDisableShield.invoke(nmsAttacker)) {
            if (this.Player_disableShield_paper != null) {
                this.Player_disableShield_paper.invoke(nmsPlayer, nmsAttacker);
            } else {
                this.Player_disableShield.invoke(nmsPlayer);
            }
        }
    }

    @Override
    protected boolean canDisableShield(LivingEntity attacker) {
        throw new AssertionError("not applicalbe for 1.20.6+");
    }
}
