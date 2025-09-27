package org.vivecraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.Platform;

public class EntityEvents implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void makeVrCompatible(CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            // replace the entity on next tick,
            // creakings set some stuff immediately after the spawn to the original entity,
            // and we wouldn't get that info elsewise
            Platform.getInstance().getScheduler().runEntityDelayed(event.getEntity(), () -> {
                if (event.getEntity().isValid()) {
                    ViveMain.NMS.modifyEntity(event.getEntity());
                }
            }, 1);
        }
    }
}
