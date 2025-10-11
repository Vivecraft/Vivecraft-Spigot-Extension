package org.vivecraft.debug;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.data.VrPlayerState;
import org.vivecraft.util.AABB;
import org.vivecraft.util.Headshot;
import org.vivecraft.util.MCVersion;
import org.vivecraft.util.MathUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Debug {

    private final static Vector RED = new Vector(1, 0, 0);
    private final static Vector GREEN = new Vector(0, 1, 0);
    private final static Vector BLUE = new Vector(0, 0, 1);

    public static void log(String msg, Object... args) {
        // this is called during config init, so it is null there
        if (ViveMain.CONFIG != null && ViveMain.CONFIG.debug.get()) {
            if (args != null && args.length > 0) {
                Object[] s = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    s[i] = String.valueOf(args[i]);
                }
                ViveMain.LOGGER.info(String.format(msg, s));
            } else {
                ViveMain.LOGGER.info(msg);
            }
        }
    }

    public static void debugParticles() {
        for (World w : Bukkit.getWorlds()) {
            Set<Entity> set = new HashSet<>();
            for (Player player : w.getPlayers()) {
                if (ViveMain.CONFIG.debugParticlesOpOnly.get() == player.isOp() || player.isOp()) {
                    for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                        spawnParticles(set, w, e);
                    }
                    spawnParticles(set, w, player);
                    if (!set.contains(player)) {
                        spawnAABBParticles(w, Headshot.getHeadHitbox(player));
                    }
                    set.add(player);
                }
            }
        }
    }

    private static void spawnParticles(Set<Entity> set, World world, Entity entity) {
        if (set.contains(entity)) return;

        if (ViveMain.CONFIG.debugParticlesHeadHitbox.get()) {
            spawnAABBParticles(world, Headshot.getHeadHitbox(entity));
        }

        if (ViveMain.isVivePlayer(entity)) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer((Player) entity);
            VrPlayerState vrPlayerState = vivePlayer.vrPlayerState();
            if (vivePlayer.isVR() && vrPlayerState != null) {

                if (ViveMain.CONFIG.debugParticlesVrDevice.get()) {
                    for (VRBodyPart bodyPart : VRBodyPart.values()) {
                        if (bodyPart.availableInMode(vrPlayerState.fbtMode) && bodyPart != VRBodyPart.HEAD) {
                            debugParticleAxes(
                                entity.getWorld(),
                                vivePlayer.getBodyPartPos(bodyPart),
                                vrPlayerState.getBodyPartPose(bodyPart).orientation);
                        }
                    }
                }
                if (ViveMain.CONFIG.debugParticlesVrHead.get()) {
                    debugParticleAxes(
                        entity.getWorld(),
                        vivePlayer.getHMDPos(),
                        vivePlayer.vrPlayerState().hmd.orientation);
                }
            }
        }
        set.add(entity);
    }

    /**
     * spawns particles for the given position and rotation
     *
     * @param world    world to spawn the particles in
     * @param position origin of the device
     * @param rot      rotation of the device
     */
    public static void debugParticleAxes(World world, Vector position, Quaternionfc rot) {
        Vector3f forward = rot.transform(MathUtils.BACK, new Vector3f());
        Vector3f up = rot.transform(MathUtils.UP, new Vector3f());
        Vector3f right = rot.transform(MathUtils.RIGHT, new Vector3f());

        spawnParticlesDirection(world, BLUE, position, forward);
        spawnParticlesDirection(world, GREEN, position, up);
        spawnParticlesDirection(world, RED, position, right);
    }

    /**
     * spawns particles with the given {@code color} at the given {@code position} in the given {@code direction}
     *
     * @param world     world to spawn the particles in
     * @param color     color of the particles
     * @param position  position to spawn the particles at
     * @param direction direction ot spawn the particles to
     */
    public static void spawnParticlesDirection(World world, Vector color, Vector position, Vector3f direction) {
        for (int i = 0; i < 5; i++) {
            // pre 1.13 the particles are regular size
            Vector3f offset = direction.mul((MCVersion.getCurrent().major < 13 ? 0.5F : 0.25F) / 4F * i,
                new Vector3f());
            ViveMain.API.spawnParticle(Particles.DEBUG, world,
                new Vector(position.getX() + offset.x, position.getY() + offset.y, position.getZ() + offset.z), 1,
                color, 0, false, null);
        }
    }

    /**
     * spawns particles at the corners of the given AABB
     *
     * @param world world to spawn the particles in
     * @param aabb  AABB to spawn the particles for
     */
    private static void spawnAABBParticles(World world, AABB aabb) {
        if (aabb == null) return;
        List<Vector> vs = new ArrayList<>();
        vs.add(new Vector(aabb.minX, aabb.minY, aabb.minZ));
        vs.add(new Vector(aabb.minX, aabb.maxY, aabb.minZ));
        vs.add(new Vector(aabb.maxX, aabb.minY, aabb.minZ));
        vs.add(new Vector(aabb.maxX, aabb.maxY, aabb.minZ));
        vs.add(new Vector(aabb.minX, aabb.minY, aabb.maxZ));
        vs.add(new Vector(aabb.minX, aabb.maxY, aabb.maxZ));
        vs.add(new Vector(aabb.maxX, aabb.minY, aabb.maxZ));
        vs.add(new Vector(aabb.maxX, aabb.maxY, aabb.maxZ));
        for (Vector v : vs) {
            ViveMain.API.spawnParticle(Particles.DEBUG, world, v, 1, RED, 0, true, null);
        }
    }
}
