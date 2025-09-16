package org.vivecraft.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3f;
import org.vivecraft.util.AABB;

/**
 * for calls that are missing an api in any version
 */
public interface NMSHelper {

    /**
     * yaw of the entities body
     *
     * @param entity entity to check
     * @return yaw of the entity
     * @apiNote since 1.12 spigot returns the head yaw in the location
     */
    float getLivingEntityBodyYaw(LivingEntity entity);

    /**
     * entity to get the view vector from
     *
     * @param entity entity to get the view vector from
     * @return look direction of the Entity
     * @apiNote technically spigot has the view direction in the Location since 1.12
     * but can never know if the y decide to change it gain
     */
    Vector3f getViewVector(Entity entity);

    /**
     * get the AABB of the entity, converted to our AABB for simplicity
     *
     * @param entity entity to get teh AABB from
     * @return AABB of the entity
     */
    AABB getEntityAABB(Entity entity);

    /**
     * sets the translation key with fallback
     *
     * @param itemStack      ItemStack to modify
     * @param translationKey key of the translation
     * @param fallback       fallback if translation is not available
     * @return the modified ItemStack
     * @apiNote available since 1.19.4, old versions just set the fallback
     */
    ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback);
}
