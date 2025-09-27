package org.vivecraft.compat;

import org.bukkit.entity.Entity;

public interface Scheduler {

    void runEntity(Entity entity, Runnable runnable);

    void runEntityDelayed(Entity entity, Runnable runnable, long delay);

    void runGlobal(Runnable runnable);

    void runGlobalDelayed(Runnable runnable, long delay);

    Task runGlobalRepeating(Runnable runnable, long delay, long period);

    void runAsync(Runnable runnable);
}
