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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.debug.Debug;
import org.vivecraft.network.NetworkHandler;
import org.vivecraft.network.packet.s2c.DamageDirectionPayloadS2C;
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
                    blockedDamageCase = ViveMain.translate("vivecraft.pvp.canceled",
                        ViveMain.translate("vivecraft.mode.nonVR"),
                        ViveMain.translate("vivecraft.mode.seatedVR"));
                }
            } else if ((!otherVive.isVR() && thisVive.isVR() && !thisVive.isSeated()) ||
                (!thisVive.isVR() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // nonvr vs Standing
                if (!ViveMain.CONFIG.pvpVrVsNonvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = ViveMain.translate("vivecraft.pvp.canceled",
                        ViveMain.translate("vivecraft.mode.nonVR"),
                        ViveMain.translate("vivecraft.mode.standingVR"));
                }
            } else if ((otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) ||
                (thisVive.isVR() && thisVive.isSeated() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // Standing vs Seated
                if (!ViveMain.CONFIG.pvpVrVsSeatedvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = ViveMain.translate("vivecraft.pvp.canceled",
                        ViveMain.translate("vivecraft.mode.seatedVR"),
                        ViveMain.translate("vivecraft.mode.standingVR"));
                }
            } else if (otherVive.isVR() && !otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) {
                // Standing vs Standing
                if (!ViveMain.CONFIG.pvpVrVsVr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = ViveMain.translate("vivecraft.pvp.canceled",
                        ViveMain.translate("vivecraft.mode.standingVR"),
                        ViveMain.translate("vivecraft.mode.standingVR"));
                }
            } else if (otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && thisVive.isSeated()) {
                // Seated vs Seated
                if (!ViveMain.CONFIG.pvpSeatedvrVsSeatedvr.get()) {
                    blockedDamage = true;
                    blockedDamageCase = ViveMain.translate("vivecraft.pvp.canceled",
                        ViveMain.translate("vivecraft.mode.seatedVR"),
                        ViveMain.translate("vivecraft.mode.seatedVR"));
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
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!ViveMain.isVRPlayer(player)) return;
        if (!ViveMain.CONFIG.allowRoomscaleShieldBlocking.get()) return;
        if (player.isBlocking()) return;

        Entity damager = event.getDamager();
        VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
        boolean isProjectile = damager instanceof Projectile;

        AABB damagerAABB = ViveMain.API.getEntityAABB(damager);
        Vector travelDir = damager.getVelocity().clone().normalize();
        Vector damagePos = damagerAABB.getCenter();

        if (isProjectile) {
            Arrow arrow = (damager instanceof Arrow) ? (Arrow) damager : null;
            if (arrow != null && ViveMain.API.isArrowPiercing(arrow)) {
                return;
            }

            float halfWidth = ViveMain.API.getEntityAABB(player).getWidth() * 0.5F;
            float distanceToPlayer = (float) damagePos.clone()
                .subtract(ViveMain.API.getEntityAABB(player).getCenter())
                .dot(travelDir);
            damagePos.add(travelDir.clone().multiply(-distanceToPlayer - halfWidth - 1.5F));
        } else {
            damagePos.subtract(travelDir);
        }

        for (int i = 0; i < 2; i++) {
            VRBodyPart hand = (i == 0) ? VRBodyPart.MAIN_HAND : VRBodyPart.OFF_HAND;
            ItemStack stack = ViveMain.API.getHandItem(player, hand);
            if (stack == null) continue;
            if (!ViveMain.API.isShield(stack)) continue;
            if (ViveMain.API.hasItemCooldown(player, stack)) continue;

            Vector3fc sideDir;
            if (vivePlayer.isLeftHanded()) {
                sideDir = (hand == VRBodyPart.MAIN_HAND) ? MathUtils.RIGHT : MathUtils.LEFT;
            } else {
                sideDir = (hand == VRBodyPart.MAIN_HAND) ? MathUtils.LEFT : MathUtils.RIGHT;
            }
            Vector3fc shieldDir = vivePlayer.getBodyPartVectorCustom(hand, sideDir);

            double angle;
            if (isProjectile) {
                Vector toHand = damagePos.clone().subtract(vivePlayer.getBodyPartPos(hand)).normalize();
                angle = shieldDir.dot((float) toHand.getX(), (float) toHand.getY(), (float) toHand.getZ());
            } else {
                Vector toPlayer = damagePos.clone().subtract(player.getLocation().toVector()).setY(0).normalize();
                Vector3f horizontalShield = new Vector3f(shieldDir.x(), 0, shieldDir.z()).normalize();
                angle = horizontalShield.dot((float) toPlayer.getX(), (float) toPlayer.getY(), (float) toPlayer.getZ());
            }

            if (angle <= 0.5) continue;

            boolean shieldBroken = false;
            if (event.getDamage() >= 3) {
                int durabilityDamage = 1 + (int) Math.floor(event.getDamage());
                shieldBroken = ViveMain.API.addDamage(stack, durabilityDamage);
                if (shieldBroken) {
                    ViveMain.API.breakItem(player, hand);
                } else {
                    ViveMain.NMS.playShieldBlockSound(player, stack);
                }
            }
            boolean applyCooldown = false;

            if (ViveMain.CONFIG.shieldCooldownRequiresAxe.get()) {
                if (damager instanceof Player) {
                    Player attacker = (Player) damager;
                    ItemStack weapon = attacker.getItemInHand();
                    if (weapon != null && weapon.getType().name().contains("_AXE")) {
                        applyCooldown = true;
                    }
                }
            } else {
                if (ViveMain.CONFIG.shieldCooldownEnabled.get()
                    && event.getDamage() >= ViveMain.CONFIG.shieldCooldownMinDamage.get()) {
                    applyCooldown = true;
                }
            }
            if (applyCooldown && !shieldBroken) {
                ViveMain.API.applyItemCooldown(player, stack, ViveMain.CONFIG.shieldCooldownTicks.get());
            }

            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void damageDirection(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && ViveMain.isVRPlayer(event.getEntity())) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(event.getEntity());
            if (vivePlayer.wantsDamageDirection) {
                Vector3fc direction = MathUtils.ZERO;
                switch (event.getCause()) {
                    case FALLING_BLOCK:
                        // damage from the above
                        direction = MathUtils.UP;
                        break;
                    case FALL:
                        // damage from the below
                        direction = MathUtils.DOWN;
                        break;
                    case PROJECTILE:
                    case ENTITY_ATTACK:
                        // just make sure that this is te case, it should be, but maybe some implementations don't do that
                        if (event instanceof EntityDamageByEntityEvent) {
                            direction = MathUtils.subToJomlVec(
                                ((EntityDamageByEntityEvent) event).getDamager().getLocation().toVector(),
                                event.getEntity().getLocation().toVector()).normalize();
                        }
                        break;
                }
                NetworkHandler.sendPacket(vivePlayer, new DamageDirectionPayloadS2C(direction));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void reducedMeleeRange(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
            ViveMain.CONFIG.mobAttackRangeAdjustment.get() < 0)
        {
            Player player = (Player) event.getEntity();
            VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
            if (vivePlayer != null && vivePlayer.isVR() && !vivePlayer.isSeated() &&
                !ViveMain.NMS.inReducedAttackRange(player, event.getDamager()))
            {
                event.setCancelled(true);
            }
        }
    }
}
