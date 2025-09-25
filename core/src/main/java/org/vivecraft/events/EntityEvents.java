package org.vivecraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.vivecraft.ViveMain;

public class EntityEvents implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void makeVrCompatible(CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            ViveMain.NMS.modifyEntity(event.getEntity());
        }
    }
}
