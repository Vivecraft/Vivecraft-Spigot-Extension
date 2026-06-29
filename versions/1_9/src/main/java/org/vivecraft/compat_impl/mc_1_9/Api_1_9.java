package org.vivecraft.compat_impl.mc_1_9;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.compat_impl.mc_1_8.Api_1_8;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Map;

public class Api_1_9 extends Api_1_8 {

    private ReflectionMethod Player_getCooldowns;
    private ReflectionMethod ItemCooldowns_addCooldown;
    private ReflectionField ItemCooldowns_cooldowns;
    private ReflectionField ItemCooldowns_tickCount;
    private ReflectionField CooldownInstance_endTime;
    private ReflectionMethod ItemStack_getItem;

    @Override
    protected void init() {
        this.Entity_width = ReflectionField.getField(EntityMapping.FIELD_WIDTH);
        this.Player_getCooldowns = ReflectionMethod.getMethod(PlayerMapping.METHOD_GET_COOLDOWNS);
        this.ItemCooldowns_addCooldown = ReflectionMethod.getMethod(ItemCooldownsMapping.METHOD_ADD_COOLDOWN);
        this.ItemCooldowns_cooldowns = ReflectionField.getField(ItemCooldownsMapping.FIELD_COOLDOWNS);
        this.ItemCooldowns_tickCount = ReflectionField.getField(ItemCooldownsMapping.FIELD_TICK_COUNT);
        this.CooldownInstance_endTime = ReflectionField.getField(ItemCooldowns$CooldownInstanceMapping.FIELD_END_TIME);
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
    }

    @Override
    public <T> void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force, T pData)
    {
        if (particle == Particles.CRIT) {
            world.spawnParticle(Particle.CRIT, pos.getX(), pos.getY(), pos.getZ(), count,
                data.getX(), data.getY(), data.getZ(), speed);
        } else if (particle == Particles.DEBUG) {
            world.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 0,
                data.getX(), data.getY(), data.getZ(), 1);
        } else if (particle == Particles.ITEM_BREAK && pData instanceof ItemStack) {
            world.spawnParticle(Particle.ITEM_CRACK, pos.getX(), pos.getY(), pos.getZ(), count,
                data.getX(), data.getY(), data.getZ(), speed, pData);
        }
    }

    @Override
    public Sound getBreakingSound() {
        return Sound.ENTITY_ITEM_BREAK;
    }

    @Override
    public boolean hasSmallHitbox(Player player) {
        return super.hasSmallHitbox(player) || player.isGliding();
    }

    @Override
    public ItemStack createItemStack(Item item, String translationName, String fallbackName, ItemFlag[] itemFlags) {
        ItemStack stack;
        switch (item) {
            case LEATHER_BOOTS:
                stack = new ItemStack(Material.LEATHER_BOOTS);
                break;
            case PUMPKIN_PIE:
                stack = new ItemStack(Material.PUMPKIN_PIE);
                break;
            case SHEARS:
                stack = new ItemStack(Material.SHEARS);
                break;
            case WATER_POTION:
                stack = new ItemStack(Material.POTION);
                break;
            default:
                throw new IllegalArgumentException("Unknown item: " + item);
        }
        ItemMeta meta = stack.getItemMeta();
        if (item == Item.WATER_POTION) {
            ((PotionMeta) meta).setBasePotionData(new PotionData(PotionType.WATER));
        }
        meta.setDisplayName(fallbackName);
        if (itemFlags != null) {
            for (ItemFlag itemFlag : itemFlags) {
                meta.addItemFlags(itemFlag);
            }
        }
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public ItemStack getHandItem(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.MAIN_HAND) {
            return player.getInventory().getItemInMainHand();
        } else if (hand == VRBodyPart.OFF_HAND) {
            return player.getInventory().getItemInOffHand();
        }
        return null;
    }

    @Override
    public void setHandItem(Player player, VRBodyPart hand, @Nullable ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = new ItemStack(Material.AIR);
        }
        if (hand == VRBodyPart.MAIN_HAND) {
            player.getInventory().setItemInMainHand(itemStack);
        } else if (hand == VRBodyPart.OFF_HAND) {
            player.getInventory().setItemInOffHand(itemStack);
        }
    }

    @Override
    public boolean hasItemCooldown(Player player, ItemStack itemStack) {
        Object nmsStack = BukkitReflector.getItemHandle(itemStack);
        if (nmsStack != null) {
            Object nmsItem = this.ItemStack_getItem.invoke(nmsStack);
            Object nmsPlayer = BukkitReflector.getEntityHandle(player);
            Object cooldowns = this.Player_getCooldowns.invoke(nmsPlayer);
            Object cooldownInstance = ((Map<?, ?>) this.ItemCooldowns_cooldowns.get(cooldowns)).get(nmsItem);
            return cooldownInstance != null && ((int) this.CooldownInstance_endTime.get(cooldownInstance) -
                (int) this.ItemCooldowns_tickCount.get(cooldowns) > 0
            );
        }
        return false;
    }

    @Override
    public void applyItemCooldown(Player player, ItemStack itemStack, int ticks, boolean force) {
        Object nmsStack = BukkitReflector.getItemHandle(itemStack);
        if (nmsStack != null) {
            Object nmsItem = this.ItemStack_getItem.invoke(nmsStack);
            Object nmsPlayer = BukkitReflector.getEntityHandle(player);
            Object cooldowns = this.Player_getCooldowns.invoke(nmsPlayer);
            Object cooldownInstance = ((Map<?, ?>) this.ItemCooldowns_cooldowns.get(cooldowns)).get(nmsItem);
            if (cooldownInstance == null || ((int) this.CooldownInstance_endTime.get(cooldownInstance) -
                (int) this.ItemCooldowns_tickCount.get(cooldowns) < ticks
            ))
            {
                this.ItemCooldowns_addCooldown.invoke(cooldowns, nmsItem, ticks);
                ViveMain.NMS.playShieldDisableSound(player, itemStack);
            }
        }
    }
}
