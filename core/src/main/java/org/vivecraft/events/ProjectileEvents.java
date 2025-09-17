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
                Vector projDir = proj.getVelocity();
                float yView = (float) Math.atan2(-view.x(), view.z());
                float yProj = (float) Math.atan2(-projDir.getX(), projDir.getZ());

                float multishotOffset = yProj - yView;

                Quaternionf inaccuracy = new Quaternionf(vivePlayer.getAimOrientation(true))
                    .rotateY(multishotOffset);

                Vector pos = vivePlayer.getAimPos(true);
                Vector aim = MathUtils.toBukkitVec(inaccuracy.transform(MathUtils.BACK, new Vector3f()));

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
                proj.teleport(loc); //paper sets velocity to 0 on teleport.
                proj.setVelocity(aim.multiply(velo * velocity));
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
                            arrow.getVelocity().multiply(-1), 0.1, true);
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
