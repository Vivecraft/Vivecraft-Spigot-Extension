package org.vivecraft.compat;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.types.Item;
import org.vivecraft.compat.types.Particles;
import org.vivecraft.util.AABB;

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
     * @param pData    additional particle data, fo the break item particle
     * @apiNote spigot has a call for that since 1.9
     */
    <T> void spawnParticle(
        Particles particle, World world, Vector pos, int count, Vector data, double speed, boolean force, T pData);

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

    /**
     * applies the spigot armor attribute modifiers
     * this only applied the spigot attributes, use in combination with {@link NMSHelper#getArmorValue(ItemStack)}
     *
     * @param baseArmor base armor value without spigot attributes
     * @param itemStack ItemStack to get the modifers from
     * @apiNote api only exists since 1.13.2
     */
    double applyArmorModifiers(double baseArmor, ItemStack itemStack);

    /**
     * get the AABB of the entity, converted to our AABB for simplicity
     *
     * @param entity entity to get the AABB from
     * @return AABB of the entity
     * @apiNote api only exists since 1.13.2
     */
    AABB getEntityAABB(Entity entity);

    /**
     * gets the item from the specific hand, if available
     *
     * @param player Player to get the item from
     * @param hand   Hand to get
     * @return hand ItemStack, or {@code null} if not available
     * @apiNote dual wielding is only a thing since 1.9
     */
    @Nullable
    ItemStack getHandItem(Player player, VRBodyPart hand);

    /**
     * sets the item from the specific hand
     *
     * @param player    Player to set the item from
     * @param hand      Hand to set
     * @param itemStack ItemStack to set, if {@code null} it will be cleared
     * @apiNote dual wielding is only a thing since 1.9
     */
    void setHandItem(Player player, VRBodyPart hand, @Nullable ItemStack itemStack);

    /**
     * return true if the given ItemStack is a shield
     *
     * @param itemStack ItemStack to check
     * @return if the item is a shield
     * @apiNote shields exist since 1.9
     */
    boolean isShield(ItemStack itemStack);

    /**
     * checks if the player has a cooldown for the given item
     *
     * @param player    Player to check for
     * @param itemStack ItemStack to check
     * @return if the item is on cooldown
     * @apiNote spigot has an api for it since 1.11.2
     */
    boolean hasItemCooldown(Player player, ItemStack itemStack);

    /**
     * checks if the arrow has piecing enchant
     *
     * @param arrow Arrow to check
     * @return if the arrow is piecing
     * @apiNote available since mc 1.14 and spigot 1.14.4
     */
    boolean isArrowPiercing(Arrow arrow);

    /**
     * adds the given damage to the ItemStack
     *
     * @param itemStack ItemStack to damage
     * @param damage    damage to add
     * @return if the damage is now above the max damage
     */
    boolean addDamage(ItemStack itemStack, int damage);

    /**
     * breaks the ItemsStack in the given hand and removes it from the players inventory
     *
     * @param player to break the ItemStack of
     * @param hand   hand to break the ItemStack in
     * @apiNote there is no native call for this, need to do it manually
     */
    void breakItem(Player player, VRBodyPart hand);
}
