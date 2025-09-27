package org.vivecraft.compat;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.compat_impl.BukkitPlatform;

import java.lang.reflect.InvocationTargetException;

public abstract class Platform {

    private static Platform INSTANCE;

    private final Scheduler scheduler;

    public Platform(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Platform getInstance() {
        if (INSTANCE == null) {
            try {
                // recommended check for folia https://docs.papermc.io/paper/dev/folia-support/
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                ViveMain.LOGGER.info("running on folia");
                INSTANCE = (Platform) Class.forName("org.vivecraft.compat_impl.FoliaPlatform")
                    .getConstructor().newInstance();
            } catch (ClassNotFoundException e) {
                INSTANCE = new BukkitPlatform();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Vivecraft: failed to get folia scheduler", e);
            }
        }
        return INSTANCE;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public abstract void teleportEntity(Entity entity, Location location, Vector velocity);
}
