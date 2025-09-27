package org.vivecraft.compat_impl;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.vivecraft.compat.Platform;

public class FoliaPlatform extends Platform {

    public FoliaPlatform() {
        super(new FoliaScheduler());
    }

    @Override
    public void teleportEntity(Entity entity, Location location, Vector velocity) {
        entity.teleportAsync(location);
        entity.setVelocity(velocity);
    }
}
