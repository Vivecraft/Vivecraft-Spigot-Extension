package org.vivecraft.compat_impl.mc_1_13;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.compat_impl.mc_1_12_2.Api_1_12_2;

public class Api_1_13 extends Api_1_12_2 {

    @Override
    public boolean hasSmallHitbox(Player player) {
        return super.hasSmallHitbox(player) || player.isSwimming();
    }

    @Override
    public <T> void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force, T pData)
    {
        if (particle != Particles.DEBUG) {
            super.spawnParticle(particle, world, pos, count, data, speed, force, pData);
        } else {
            world.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB((int) (data.getX() * 255.0), (int) (data.getY() * 255.0),
                    (int) (data.getZ() * 255.0)), 0.25F));
        }
    }

    @Override
    public boolean addDamage(ItemStack itemStack, int damage) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();
            int durability = damageable.getDamage();
            int newDurability = durability + damage;
            damageable.setDamage(newDurability);
            itemStack.setItemMeta((ItemMeta) damageable);
            return newDurability >= itemStack.getType().getMaxDurability();
        } else {
            return false;
        }
    }
}
