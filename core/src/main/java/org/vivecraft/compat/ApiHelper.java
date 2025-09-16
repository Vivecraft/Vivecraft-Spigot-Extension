package org.vivecraft.compat;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;

/**
 * handles calls that have a bukkit api call in new versions, but not the old ones
 */
public interface ApiHelper {

    /**
     * spawns CRIT particles at the given location for the given player
     *
     * @param particle particle to spawn
     * @param world    the world to spawn the particle in
     * @param pos      position to spawn the particles at
     * @param count    number of particles
     * @param data     data for the particle, for CRIT this is the direction, for DEBUG this is the Color
     * @param speed    speed of the particles
     * @param force    if the particles should always try to render
     * @apiNote spigot has a call for that since 1.9
     */
    void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force);

    /**
     * gets the item breaking sound
     *
     * @return returns the Sound object corresponding to the item breaking sound
     * @apiNote spigot changed the name of the Sound event in
     */
    Sound getBreakingSound();

    /**
     * checks if the given player has a small hitbox
     * checks for sleeping, elytra flying, swimming
     *
     * @param player player to check
     * @return if the player has a small hitbox
     */
    boolean hasSmallHitbox(Player player);

    /**
     * gets the width of the entities bounding box
     *
     * @param entity entity to check
     * @return width of the entity
     * @apiNote spigot has a call for that since 1.12
     */
    float getEntityWidth(Entity entity);

    /**
     * creates an ItemStack for the given item, with the given attributes
     *
     * @param item            Item to create
     * @param translationName translation string if available
     * @param fallbackName    plain fallback name when no translation is available
     * @param itemFlags       flags to hide info
     * @return the created ItemStack
     */
    ItemStack createItemStack(
        Item item, @Nullable String translationName, String fallbackName, @Nullable ItemFlag[] itemFlags);

    /**
     * sets the given ItemStack to be unbreakable
     *
     * @param itemStack ItemStack to modify
     * @param hide      if the unbreakable flag should be hidden in the tooltip
     * @return if the action was successful
     */
    boolean setItemStackUnbreakable(ItemStack itemStack, boolean hide);

    /**
     * creates a ShapedRecipe object for the give nItemStack
     *
     * @param itemStack ItemStack to create teh recipe for
     * @param id        recipe id to apply
     */
    ShapedRecipe createRecipe(ItemStack itemStack, String id);

    /**
     * gets the name of the causing entity
     *
     * @param event Event to get the causing entity from
     * @return name of the causing entity, or an empty string if there is none
     * @apiNote damage source cause is only available since 1.20.6
     */
    String getCausingEntityName(PlayerDeathEvent event);
}
