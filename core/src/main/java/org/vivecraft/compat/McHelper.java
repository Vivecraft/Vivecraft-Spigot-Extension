package org.vivecraft.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * handles calls that depends on items/blocks/entities that are added in later versions
 */
public interface McHelper {

    boolean isArrow(Entity entity);

    /**
     * checks if the given entity human like, so the head is in the center of the hitbox
     *
     * @param entity Entity to check
     * @return if the entity is human like
     */
    boolean hasHumanoidHead(LivingEntity entity);

    /**
     * checks if the given entity is animal like, so the head in front of the hitbox
     *
     * @param entity Entity to check
     * @return if the entity is animal like
     */
    boolean hasAnimalHead(LivingEntity entity);
}
