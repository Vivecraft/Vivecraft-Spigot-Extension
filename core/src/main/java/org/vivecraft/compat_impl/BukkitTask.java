package org.vivecraft.compat_impl;

import org.vivecraft.compat.Task;

public class BukkitTask implements Task {

    private final org.bukkit.scheduler.BukkitTask task;

    public BukkitTask(org.bukkit.scheduler.BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }
}
