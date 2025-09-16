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
import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.compat_impl.mc_1_8.Api_1_8;
import org.vivecraft.util.reflection.ReflectionField;

public class Api_1_9 extends Api_1_8 {

    @Override
    protected void init() {
        this.Entity_width = ReflectionField.getField(EntityMapping.FIELD_WIDTH);
    }

    @Override
    public void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force)
    {
        if (particle == Particles.CRIT) {
            world.spawnParticle(Particle.CRIT, pos.getX(), pos.getY(), pos.getZ(), count,
                data.getX(), data.getY(), data.getZ(), speed);
        } else if (particle == Particles.DEBUG) {
            world.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 0,
                data.getX(), data.getY(), data.getZ(), 1);
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
}
