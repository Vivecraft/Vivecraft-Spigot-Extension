package org.vivecraft.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;

public class PvPEvents implements Listener {
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        // we only care about player vs player events
        if (!(event.getEntity() instanceof Player)) return;

        Entity entity = event.getDamager();
        Player other = null;

        // check if the damage came from another player
        if (entity instanceof Player) {
            other = (Player) entity;
        } else if ((entity instanceof Projectile && (((Projectile) entity).getShooter() instanceof Player))) {
            other = (Player) ((Projectile) entity).getShooter();
        }

        if (other != null) {
            // both entities are players, so need to check

            VivePlayer otherVive = ViveMain.getVivePlayer(other);
            VivePlayer thisVive = ViveMain.getVivePlayer((Player) event.getEntity());

            // create new object, if they are null, simplifies the checks
            if (otherVive == null) {
                otherVive = new VivePlayer(other);
            }

            if (thisVive == null) {
                thisVive = new VivePlayer((Player) event.getEntity());
            }

            boolean blockedDamage = false;
            String blockedDamageCase = "";

            if ((!otherVive.isVR() && thisVive.isVR() && thisVive.isSeated()) ||
                (!thisVive.isVR() && otherVive.isVR() && otherVive.isSeated()))
            {
                // nonvr vs Seated
                if (!ViveMain.CONFIG.pvpSeatedvrVsNonvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs seated VR damage";
                }
            } else if ((!otherVive.isVR() && thisVive.isVR() && !thisVive.isSeated()) ||
                (!thisVive.isVR() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // nonvr vs Standing
                if (!ViveMain.CONFIG.pvpVrVsNonvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs standing VR damage";
                }
            } else if ((otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) ||
                (thisVive.isVR() && thisVive.isSeated() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // Standing vs Seated
                if (!ViveMain.CONFIG.pvpVrVsSeatedvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && !otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) {
                // Standing vs Standing
                if (!ViveMain.CONFIG.pvpVrVsVr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled standing VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && thisVive.isSeated()) {
                // Seated vs Seated
                if (!ViveMain.CONFIG.pvpSeatedvrVsSeatedvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs seated VR damage";
                }
            }
            if (blockedDamage) {
                if (ViveMain.CONFIG.pvpNotifyBlockedDamage.get()) {
                    other.sendMessage(blockedDamageCase);
                }
                event.setCancelled(true);
            }
        }
    }
}
