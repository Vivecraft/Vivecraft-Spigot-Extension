package org.vivecraft.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.debug.Debug;

public class DamageEvents implements Listener {
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

    @EventHandler
    public void noAttackWhileBlocking(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && ViveMain.isVRPlayer(event.getDamager())) {
            Player player = (Player) event.getDamager();
            if (player.isBlocking() && !ViveMain.CONFIG.allowAttacksWhileBlocking.get()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void bootsDamage(EntityDamageByEntityEvent event) {
        // feet make more damage with boots
        if (ViveMain.CONFIG.dualWielding.get() && ViveMain.CONFIG.bootsArmorDamage.get() > 0) {
            if (event.getDamager() instanceof Player && ViveMain.isVRPlayer(event.getDamager())) {
                Player player = (Player) event.getDamager();
                VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
                ItemStack itemStack = player.getEquipment().getBoots();
                if (vivePlayer.activeBodyPart.isFoot() && itemStack != null && itemStack.getType() != Material.AIR) {
                    double armor = ViveMain.API.applyArmorModifiers(ViveMain.NMS.getArmorValue(itemStack),
                        player.getInventory().getBoots());
                    double addedDamage = armor * ViveMain.CONFIG.bootsArmorDamage.get();
                    Debug.log("Boots hit: armor level: %s, total damage added: %s", armor, addedDamage);
                    event.setDamage(event.getDamage() + addedDamage);
                }
            }
        }
    }

    @EventHandler
    public void roomscaleBlocking(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && ViveMain.isVRPlayer(event.getEntity())) {
            Player player = (Player) event.getEntity();

            if (player.isBlocking() && !ViveMain.CONFIG.allowAttacksWhileBlocking.get()) {
                event.setCancelled(true);
            }
        }
    }
}
