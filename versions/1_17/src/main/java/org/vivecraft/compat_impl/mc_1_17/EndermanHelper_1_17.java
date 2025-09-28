package org.vivecraft.compat_impl.mc_1_17;

import org.vivecraft.accessors.*;
import org.vivecraft.compat.entities.EndermanLookForPlayerGoalAccessor;
import org.vivecraft.compat_impl.mc_1_16.EndermanHelper_1_16;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;
import java.util.function.Predicate;

public class EndermanHelper_1_17 extends EndermanHelper_1_16 {

    protected ReflectionMethod ItemStack_Is;
    protected ReflectionMethod Block_asItem;
    protected ReflectionField Blocks_CARVED_PUMPKIN;

    protected ReflectionMethod TargetingConditions_forCombat;

    @Override
    protected void initFindPlayer() {
        super.initFindPlayer();
        this.TargetingConditions_forCombat = ReflectionMethod.getMethod(TargetingConditionsMapping.METHOD_FOR_COMBAT);
    }

    @Override
    protected void initInventory() {
        this.Player_inventory = ReflectionField.getField(PlayerMapping.FIELD_INVENTORY);
        this.Inventory_armor = ReflectionField.getField(InventoryMapping.FIELD_ARMOR_1);
        this.ItemStack_Is = ReflectionMethod.getMethod(ItemStackMapping.METHOD_IS);
        this.Block_asItem = ReflectionMethod.getMethod(BlockMapping.METHOD_AS_ITEM);
        this.Blocks_CARVED_PUMPKIN = ReflectionField.getField(BlocksMapping.FIELD_CARVED_PUMPKIN);
    }

    @Override
    public void lookForPlayerInit(EndermanLookForPlayerGoalAccessor goal, double distance) {
        goal.setContinueAggroConditions(
            this.TargetingConditions_allowUnseeable.invoke(this.TargetingConditions_forCombat.invokes()));
        goal.setStartAggroConditions(this.TargetingConditions_selector.invoke(
            this.TargetingConditions_range.invoke(this.TargetingConditions_forCombat.invokes(), distance),
            (Predicate) (player) -> isAngerInducing(player, goal.getEnderman())));
    }

    @Override
    public boolean hasProtection(Object nmsPlayer) {
        Object headitem = ((List) this.Inventory_armor.get(this.Player_inventory.get(nmsPlayer))).get(3);
        return (boolean) this.ItemStack_Is.invoke(headitem, this.Block_asItem.invoke(this.Blocks_CARVED_PUMPKIN.get()));
    }

    protected boolean isAngerInducing(Object target, Object enderman) {
        return canPlayerSeeEnderman(target, enderman);
    }
}
