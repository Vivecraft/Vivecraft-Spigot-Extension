package org.vivecraft.events;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.AABB;
import org.vivecraft.util.MathUtils;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void roomscaleBlocking(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && ViveMain.isVRPlayer(event.getEntity()) &&
            ViveMain.CONFIG.allowRoomscaleShieldBlocking.get())
        {
            Player player = (Player) event.getEntity();
            if (!player.isBlocking()) {
                Entity damage = event.getDamager();
                VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
                boolean isProjectile = false;
                if (damage instanceof Projectile) {
                    isProjectile = true;
                    if (damage instanceof Arrow && ViveMain.API.isArrowPiercing((Arrow) damage)) {
                        // can't block piercing arrows
                        return;
                    }
                }

                // move it back in the movement direction, to get a better source direction
                AABB bb = ViveMain.API.getEntityAABB(damage);
                Vector dmgPos = bb.getCenter().subtract(damage.getVelocity().normalize());

                // check if any hand is holding a shield
                for (int i = 0; i < 2; i++) {
                    VRBodyPart hand = i == 0 ? VRBodyPart.MAIN_HAND : VRBodyPart.OFF_HAND;
                    ItemStack stack = ViveMain.API.getHandItem(player, hand);

                    // check for shield and do not bypass item cooldowns
                    if (stack != null && ViveMain.API.isShield(stack) && !ViveMain.API.hasItemCooldown(player, stack)) {
                        // check if it blocks
                        Vector3fc sideDir;
                        if (vivePlayer.isLeftHanded()) {
                            sideDir = hand == VRBodyPart.MAIN_HAND ? MathUtils.RIGHT : MathUtils.LEFT;
                        } else {
                            sideDir = hand == VRBodyPart.MAIN_HAND ? MathUtils.LEFT : MathUtils.RIGHT;
                        }
                        Vector3fc shieldDir = vivePlayer.getBodyPartVectorCustom(hand, sideDir);

                        // 0.5 = 120° blocking cone
                        double angle = 0;
                        if (isProjectile) {
                            // direction to hand
                            Vector dmgDir = dmgPos.subtract(vivePlayer.getBodyPartPos(hand)).normalize();
                            angle = shieldDir.dot((float) dmgDir.getX(), (float) dmgDir.getY(), (float) dmgDir.getZ());
                        } else {
                            // horizontal direction to the player
                            Vector dmgDir = dmgPos.subtract(player.getLocation().toVector()).setY(0).normalize();
                            Vector3f hShieldDir = new Vector3f(shieldDir.x(), 0, shieldDir.z()).normalize();
                            angle = hShieldDir.dot((float) dmgDir.getX(), (float) dmgDir.getY(), (float) dmgDir.getZ());
                        }
                        if (angle > 0.5) {
                            if (event.getDamage() >= 3) {
                                // damage shield
                                // durability counts up the damage
                                if (ViveMain.API.addDamage(stack, 1 + (int) Math.floor(event.getDamage()))) {
                                    ViveMain.API.breakItem(player, hand);
                                }
                            }
                            // TODO knockback and shield disable
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
