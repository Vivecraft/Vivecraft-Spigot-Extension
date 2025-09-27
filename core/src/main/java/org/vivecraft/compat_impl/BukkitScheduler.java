package org.vivecraft.compat_impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.Scheduler;
import org.vivecraft.compat.Task;

public class BukkitScheduler implements Scheduler {
    @Override
    public void runEntity(Entity entity, Runnable runnable) {
        this.runGlobal(runnable);
    }

    @Override
    public void runEntityDelayed(Entity entity, Runnable runnable, long delay) {
        this.runGlobalDelayed(runnable, delay);
    }

    @Override
    public void runGlobal(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(ViveMain.INSTANCE, runnable);
    }

    @Override
    public void runGlobalDelayed(Runnable runnable, long delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ViveMain.INSTANCE, runnable, delay);
    }

    @Override
    public Task runGlobalRepeating(Runnable runnable, long delay, long period) {
        return new BukkitTask(
            Bukkit.getServer().getScheduler().runTaskTimer(ViveMain.INSTANCE, runnable, delay, period));
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(ViveMain.INSTANCE, runnable);
    }
}
