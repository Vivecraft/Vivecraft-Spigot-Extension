package org.vivecraft.compat_impl.mc_1_8;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.accessors.EnumParticleMapping;
import org.vivecraft.accessors.ServerLevelMapping;
import org.vivecraft.compat.ApiHelper;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.logging.Level;

public class Api_1_8 implements ApiHelper {

    protected ReflectionField Entity_width;
    protected ReflectionMethod Level_spawnParticle;
    protected ReflectionField EnumParticle_CRIT;
    protected ReflectionField EnumParticle_REDSTONE;

    public Api_1_8() {
        init();
    }

    protected void init() {
        this.Entity_width = ReflectionField.getField(EntityMapping.FIELD_WIDTH);
        this.Level_spawnParticle = ReflectionMethod.getMethod(ServerLevelMapping.METHOD_FUNC_180505_A);
        this.EnumParticle_CRIT = ReflectionField.getField(EnumParticleMapping.FIELD_CRIT);
        this.EnumParticle_REDSTONE = ReflectionField.getField(EnumParticleMapping.FIELD_REDSTONE);
    }

    @Override
    public void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force)
    {
        if (particle == Particles.CRIT) {
            this.Level_spawnParticle.invoke(BukkitReflector.getHandle(world), this.EnumParticle_CRIT.get(), force,
                pos.getX(), pos.getY(), pos.getZ(), count, data.getX(), data.getY(), data.getZ(), speed, null);
        } else if (particle == Particles.DEBUG) {
            this.Level_spawnParticle.invoke(BukkitReflector.getHandle(world), this.EnumParticle_REDSTONE.get(),
                force, pos.getX(), pos.getY(), pos.getZ(), 0, data.getX(), data.getY(), data.getZ(), 1, null);
        }
    }

    @Override
    public Sound getBreakingSound() {
        return Sound.ITEM_BREAK;
    }

    @Override
    public boolean hasSmallHitbox(Player player) {
        return player.isSleeping();
    }

    @Override
    public float getEntityWidth(Entity entity) {
        // up to including some versions of 1.11.2 there is no api for that
        return (float) this.Entity_width.get(BukkitReflector.getHandle(entity));
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
                new Potion(PotionType.WATER).apply(stack);
                break;
            default:
                throw new IllegalArgumentException("Unknown item: " + item);
        }
        ItemMeta meta = stack.getItemMeta();
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
    public boolean setItemStackUnbreakable(ItemStack itemStack, boolean hide) {
        ItemMeta meta = itemStack.getItemMeta();
        try {
            meta.spigot().setUnbreakable(true);
        } catch (NoSuchMethodError e) {
            ViveMain.LOGGER.log(Level.SEVERE, "errors setting unbreakable", e);
            return false;
        }
        if (hide) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        itemStack.setItemMeta(meta);
        return true;
    }

    @Override
    public ShapedRecipe createRecipe(ItemStack itemStack, String id) {
        return new ShapedRecipe(itemStack);
    }

    @Override
    public String getCausingEntityName(PlayerDeathEvent event) {
        return "";
    }

    @Override
    public double applyArmorModifiers(double baseArmor, ItemStack itemStack) {
        return baseArmor;
    }
}
