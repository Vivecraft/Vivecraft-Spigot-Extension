package org.vivecraft.events;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat.Platform;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.config.enums.HeadshotIndicator;
import org.vivecraft.util.Headshot;
import org.vivecraft.util.MathUtils;

public class ProjectileEvents implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void vrProjectileDirection(ProjectileLaunchEvent event) {
        //position all projectiles correctly.
        Projectile proj = event.getEntity();

        if (proj.getShooter() instanceof Player) {
            Player player = (Player) proj.getShooter();
            VivePlayer vivePlayer = ViveMain.getVivePlayer(player);
            if (vivePlayer != null && vivePlayer.isVR()) {

                final boolean arrow = ViveMain.MC.isArrow(proj);

                Vector3f view = ViveMain.NMS.getViewVector(player);
                Vector projDir = proj.getVelocity().normalize();
                Quaternionf rotation = new Quaternionf(vivePlayer.getAimOrientation(true));
                boolean isMainArrow = false;

                if (view.dot((float) projDir.getX(), (float) projDir.getY(), (float) projDir.getZ()) > 0.996F) {
                    // main arrow
                    // apply inaccuracy
                    rotation.rotateTo(
                        view.x, view.y, view.z,
                        (float) projDir.getX(), (float) projDir.getY(), (float) projDir.getZ());
                    isMainArrow = true;
                } else {
                    // multishot side arrows
                    // no inaccuracy just spread
                    float yView = (float) Math.atan2(-view.x(), view.z());
                    float yProj = (float) Math.atan2(-projDir.getX(), projDir.getZ());

                    float multishotOffset = yProj - yView;
                    rotation.rotateY(multishotOffset);
                }

                Vector pos = vivePlayer.getAimPos(true);
                Vector3f aimF = rotation.transform(MathUtils.BACK, new Vector3f());
                if (isMainArrow && ViveMain.CONFIG.projectileInaccuracyMultiplier.get() < 1.0) {
                    aimF.lerp(vivePlayer.getAimDir(true),
                        1F - ViveMain.CONFIG.projectileInaccuracyMultiplier.get().floatValue());
                }
                Vector aim = MathUtils.toBukkitVec(aimF);

                double velocity = 1.0;
                if (arrow && vivePlayer.draw != 0 && !vivePlayer.isSeated()) {
                    //this only works if the incoming speed is at max (based! on draw time)
                    //TODO: properly scale in all cases.
                    velocity = vivePlayer.draw;
                }

                Location loc = new Location(proj.getWorld(), pos.getX(), pos.getY(), pos.getZ());

                loc.setDirection(aim);
                // how the hell is this inverted? it was set by spigot based on the given direction
                loc.setPitch(-loc.getPitch());
                loc.setYaw(-loc.getYaw());

                double velo = proj.getVelocity().length();
                Platform.getInstance().teleportEntity(proj, loc, aim.multiply(velo * velocity));
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void arrowDamageMultiplier(EntityDamageByEntityEvent event) {
        if (!ViveMain.MC.isArrow(event.getDamager())) return;

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (!(arrow.getShooter() instanceof Player)) return;


            Player player = (Player) arrow.getShooter();
            VivePlayer vivePlayer = ViveMain.getVivePlayer(player);

            Vector hitPos = Headshot.isHeadshot(arrow, event.getEntity());
            double multiplier = 1;

            if (hitPos != null) {
                if (vivePlayer != null && vivePlayer.isVR()) {
                    if (vivePlayer.isSeated()) {
                        multiplier = ViveMain.CONFIG.bowSeatedHeadshotMultiplier.get();
                    } else {
                        multiplier = ViveMain.CONFIG.bowStandingHeadshotMultiplier.get();
                    }
                } else {
                    multiplier = ViveMain.CONFIG.bowVanillaHeadshotMultiplier.get();
                }

                if (multiplier > 1.0 && ViveMain.CONFIG.bowHeadshotIndicator.get() != HeadshotIndicator.NONE) {
                    if (ViveMain.CONFIG.bowHeadshotIndicator.get() != HeadshotIndicator.AUDIO) {
                        // send headshot particles
                        ViveMain.API.spawnParticle(Particles.CRIT, player.getWorld(), hitPos, 5,
                            arrow.getVelocity().multiply(-1), 0.1, true, null);
                    }
                    if (ViveMain.CONFIG.bowHeadshotIndicator.get() != HeadshotIndicator.VISUAL) {
                        // send sound effect
                        player.playSound(player.getLocation(), ViveMain.API.getBreakingSound(), 0.7F, 0.5F);
                    }
                }
            }

            if (vivePlayer != null && vivePlayer.isVR()) {
                if (vivePlayer.isSeated()) {
                    multiplier = Math.max(multiplier, ViveMain.CONFIG.bowSeatedMultiplier.get());
                } else {
                    multiplier = Math.max(multiplier, ViveMain.CONFIG.bowStandingMultiplier.get());
                }
            }
            event.setDamage(event.getDamage() * multiplier);
        }
    }
}
