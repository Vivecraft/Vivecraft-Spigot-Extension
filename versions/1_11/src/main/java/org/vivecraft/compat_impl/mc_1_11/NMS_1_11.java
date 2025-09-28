package org.vivecraft.compat_impl.mc_1_11;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.ItemStackMapping;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat_impl.mc_1_9.NMS_1_9;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.List;

public class NMS_1_11 extends NMS_1_9 {

    protected ReflectionMethod Entity_getEyePosition;
    protected ReflectionField ItemStack_EMPTY;

    @Override
    protected void init() {
        super.init();
        this.Entity_getEyePosition = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_EYE_POSITION);
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
}
