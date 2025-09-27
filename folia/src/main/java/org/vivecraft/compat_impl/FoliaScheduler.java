package org.vivecraft.compat_impl;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.Scheduler;
import org.vivecraft.compat.Task;

public class FoliaScheduler implements Scheduler {
    @Override
    public void runEntity(Entity entity, Runnable runnable) {
        entity.getScheduler().run(ViveMain.INSTANCE, t -> runIfNotCanceled(t, runnable), null);
    }

    @Override
    public void runEntityDelayed(Entity entity, Runnable runnable, long delay) {
        entity.getScheduler().execute(ViveMain.INSTANCE, runnable, null, delay);
    }

    @Override
    public void runGlobal(Runnable runnable) {
        Bukkit.getServer().getGlobalRegionScheduler().execute(ViveMain.INSTANCE, runnable);
    }

    @Override
    public void runGlobalDelayed(Runnable runnable, long delay) {
        Bukkit.getServer().getGlobalRegionScheduler()
            .runDelayed(ViveMain.INSTANCE, t -> runIfNotCanceled(t, runnable), delay);
    }

    @Override
    public Task runGlobalRepeating(Runnable runnable, long delay, long period) {
        return new FoliaTask(Bukkit.getServer().getGlobalRegionScheduler()
            .runAtFixedRate(ViveMain.INSTANCE, t -> runIfNotCanceled(t, runnable), delay, period));
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getServer().getAsyncScheduler().runNow(ViveMain.INSTANCE, t -> runIfNotCanceled(t, runnable));
    }

    private static void runIfNotCanceled(ScheduledTask task, Runnable runnable) {
        if (!task.isCancelled()) {
            runnable.run();
        }
    }
}
