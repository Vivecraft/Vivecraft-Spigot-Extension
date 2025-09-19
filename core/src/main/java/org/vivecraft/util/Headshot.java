package org.vivecraft.util;

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;

public class Headshot {

    private final static double INFLATE = 0.3;

    /**
     * checks if the given projectile hit the head of the entity, and returns the hit point if so
     *
     * @param projectile projectile that hit hte entity
     * @param target     entity that got hit
     * @return head hit point, or {@code null} if it missed the head
     */
    public static Vector isHeadshot(Projectile projectile, Entity target) {
        AABB headBox = getHeadHitbox(target);
        if (headBox != null) {
            Vector projPos = projectile.getLocation().toVector();
            Vector originalHitPos = ViveMain.API.getEntityAABB(target)
                .clip(projPos, new Vector().copy(projPos).add(projectile.getVelocity().multiply(2.0)))
                .orElse(new Vector().copy(projPos).add(projectile.getVelocity()));
            return headBox
                .clip(projPos, originalHitPos)
                .orElse(headBox.contains(projPos) ? projPos : null);
        }
        return null;
    }

    /**
     * tries to calculate the head hitbox of the given entity
     *
     * @param entity entity to get the head hitbox for
     * @return head hitbox of the given entity, or {@code null} if none is available
     */
    @Nullable
    public static AABB getHeadHitbox(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return null;
        }
        LivingEntity livingEntity = (LivingEntity) entity;
        if (ViveMain.MC.hasHumanoidHead(livingEntity)) {
            Vector headPos = livingEntity.getEyeLocation().toVector();
            double headsize = ViveMain.API.getEntityWidth(livingEntity) * 0.5;
            // babies have big heads
            if (livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult()) {
                headsize *= 1.20;
            }

            return new AABB(
                headPos.getX() - headsize, headPos.getY() - headsize + INFLATE, headPos.getZ() - headsize,
                headPos.getX() + headsize, headPos.getY() + headsize + INFLATE, headPos.getZ() + headsize)
                .inflate(INFLATE);
        } else if (ViveMain.MC.hasAnimalHead(livingEntity)) {
            Location loc = livingEntity.getEyeLocation();
            float yRot = -(ViveMain.NMS.getLivingEntityBodyYaw(livingEntity)) * MathUtils.DEG_TO_RAD;
            double entityWidth = ViveMain.API.getEntityWidth(livingEntity);

            // offset head in entity rotation
            Vector headPos = new Vector(
                loc.getX() + Math.sin(yRot) * entityWidth * 0.5,
                loc.getY(),
                loc.getZ() + Math.cos(yRot) * entityWidth * 0.5);

            double headsize = entityWidth * 0.25;
            if (livingEntity instanceof Ageable && !((Ageable) livingEntity).isAdult()) {
                // babies have big heads
                headsize *= 1.5;
            }

            return new AABB(
                headPos.getX() - headsize, headPos.getY() - headsize, headPos.getZ() - headsize,
                headPos.getX() + headsize, headPos.getY() + headsize, headPos.getZ() + headsize)
                .inflate(INFLATE * 0.25)
                .expandTowards(headPos.subtract(entity.getLocation().toVector()).multiply(INFLATE));
        }
        return null;
    }
}
