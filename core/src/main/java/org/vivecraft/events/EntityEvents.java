package org.vivecraft.events;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat.Platform;
import org.vivecraft.util.MathUtils;

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

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // only for VR players, and make sure the player is not dead, we don't want to affect the death drops
        if (ViveMain.isVRPlayer(event.getPlayer()) && event.getPlayer().isValid()) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(event.getPlayer());

            Item item = event.getItemDrop();

            Location location = item.getLocation();

            Vector pos = vivePlayer.getAimPos(false);
            location.setX(pos.getX());
            location.setY(pos.getY() - ViveMain.API.getEntityHeight(item) * 0.5F);
            location.setZ(pos.getZ());

            Vector3fc aim = vivePlayer.getAimDir(false);
            Vector velocity = MathUtils.toBukkitVec(aim).multiply(item.getVelocity().length());
            location.add(velocity);

            Platform.getInstance().teleportEntity(item, location, velocity);
        }
    }
}
