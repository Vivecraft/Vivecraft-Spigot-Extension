package org.vivecraft.compat;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.compat_impl.BukkitPlatform;

import java.lang.reflect.InvocationTargetException;

public abstract class Platform {

    public static final boolean FOLIA = isFolia();

    private static Platform INSTANCE;

    private final Scheduler scheduler;

    public Platform(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Platform getInstance() {
        if (INSTANCE == null) {
            if (FOLIA) {
                try {
                    ViveMain.LOGGER.info("running on folia");
                    INSTANCE = (Platform) Class.forName("org.vivecraft.compat_impl.FoliaPlatform").getConstructor()
                        .newInstance();
                } catch (ClassNotFoundException | InvocationTargetException |
                         InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException("Vivecraft: failed to get folia scheduler", e);
                }
            } else {
                try {
                    // check for paper async teleport
                    Entity.class.getMethod("teleportAsync", Location.class);
                    ViveMain.LOGGER.info("running on paper 1.13+");
                    INSTANCE = (Platform) Class.forName("org.vivecraft.compat_impl.PaperPlatform").getConstructor()
                        .newInstance();
                } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    INSTANCE = new BukkitPlatform();
                }
            }
        }
        return INSTANCE;
    }

    private static boolean isFolia() {
        try {
            // recommended check for folia https://docs.papermc.io/paper/dev/folia-support/
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public abstract void teleportEntity(Entity entity, Location location, @Nullable Vector velocity);
}
