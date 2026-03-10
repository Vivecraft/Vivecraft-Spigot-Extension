package org.vivecraft.compat_impl;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * same as the bukkit Platform, just uses the async teleport
 */
public class PaperPlatform extends BukkitPlatform {

    public PaperPlatform() {
        super();
    }

    @Override
    public void teleportEntity(Entity entity, Location location, Vector velocity) {
        entity.teleportAsync(location).thenAccept(success -> {
            if (velocity != null) {
                entity.setVelocity(velocity);
            }
        });
    }
}
