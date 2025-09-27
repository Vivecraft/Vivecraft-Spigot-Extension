package org.vivecraft.compat_impl;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.vivecraft.compat.Task;

public class FoliaTask implements Task {

    private final ScheduledTask scheduledTask;

    public FoliaTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void cancel() {
        this.scheduledTask.cancel();
    }
}
