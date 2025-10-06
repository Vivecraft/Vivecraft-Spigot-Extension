package org.vivecraft.compat_impl;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.vivecraft.compat.Platform;

public class BukkitPlatform extends Platform {

    public BukkitPlatform() {
        super(new BukkitScheduler());
    }

    @Override
    public void teleportEntity(Entity entity, Location location, Vector velocity) {
        entity.teleport(location); //paper sets velocity to 0 on teleport.
        if (velocity != null) {
            entity.setVelocity(velocity);
        }
    }
}
