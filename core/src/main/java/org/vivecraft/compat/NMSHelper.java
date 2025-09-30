package org.vivecraft.compat;

import io.netty.channel.Channel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.data.PlayerState;

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
     * Converts a mc Vec3 to a bukkit Vector
     */
    Vector vec3ToVector(Object vec3);

    /**
     * creates a new MC Vec3
     */
    Object newVec3(double x, double y, double z);

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

    /**
     * resets the fall distance and aboveGroundTickCount of the given Player
     */
    void resetFallDistance(Player player);

    /**
     * gets the network connection for the player
     */
    Object getConnection(Player player);

    /**
     * returns if the given connection is still connected
     */
    boolean isConnectionConnected(Object connection);

    /**
     * gets the underlying netty Channel
     */
    Channel getChannel(Object connection);

    /**
     * gets the active packet listener of the connection
     */
    Object getPacketListener(Object connection);

    /**
     * gets the server this serverPlayer is part of
     */
    Object getServer(Object serverPlayer);

    /**
     * gets the level this entity is in
     */
    Object getLevel(Object entity);

    /**
     * runs the given Runnable on the main thread of the given server
     */
    void runOnMainThread(Object server, Runnable runnable);

    /**
     * gets the current player of the PacketListener
     */
    Object getPlayer(Object packetListener);

    /**
     * handles the given packet using the given listener
     */
    void handlePacket(Object player, Object packet, Object packetListener, float xRot, float yRot);

    /**
     * checks if the Packet needs aimfix handling
     */
    boolean needsAimfixHandling(Object packet);

    /**
     * copies position and direction values from the player
     */
    PlayerState getPlayerState(Object serverPlayer);

    /**
     * overrides position and direction values of the player
     */
    void setPlayerState(Object serverPlayer, Vector position, float xRot, float yRot);

    /**
     * restores position and direction values of the player to the given state, except of the position,
     * if it doesn't match either the modified, or the original position
     */
    boolean restorePlayerState(Object serverPlayer, PlayerState original, Vector modifiedPosition);

    /**
     * gets the vanilla armor level of an item
     */
    double getArmorValue(ItemStack itemStack);

    /**
     * checks if the given nms entity is a VR player
     */
    boolean isVRPlayer(Object nmsEntity);

    /**
     * returns if the mobs target is a vr player
     */
    boolean isTargetVrPlayer(Object mob);

    /**
     * gets the corresponding vr player
     */
    VivePlayer getVRPlayer(Object nmsEntity);

    /**
     * gets the head position of the given entity, accounting for vrplayers
     */
    Vector getHeadPosVR(Object nmsEntity);

    /**
     * gets teh position of the entity
     */
    Vector getEntityPosition(Object nmsEntity);

    /**
     * gets the head direction of the given entity, accounting for vrplayers
     */
    Vector getViewVectorVR(Object nmsEntity);

    /**
     * Modifies the given entity to be VR compatible
     *
     * @param entity to modify
     */
    void modifyEntity(Entity entity);

    /**
     * checks if any blocks are in between from and to, that match the given contexes
     */
    boolean clipWorld(
        Object level, Vector from, Vector to, BlockContext block, FluidContext fluid, Object sourceEntity);

    /**
     * checks if the player can see targets head
     *
     * @param player            player the looks
     * @param target            entity to look at
     * @param tolerance         angle the player needs to look at a minimum
     * @param scaleWithDistance reduces the {@code tolerance} with distance
     * @param visualClip        checks the visual shape of blocks, instead of their collision boxes
     * @param yValues           y values at the targets X/Z to check, if not set, only checks the head
     * @return if there are no blocks in the way
     */
    boolean canSeeEachOther(
        Object player, Object target, double tolerance, boolean scaleWithDistance, boolean visualClip,
        double... yValues);

    /**
     * sets teh players pose to swimming
     *
     * @param player player to modify
     */
    void setSwimPose(Player player);

    /**
     * adds a crawl pose wrapper to the player, the sets the swim pose when crawling
     *
     * @param player player to modify
     */
    void addCrawlPoseWrapper(Player player);

    /**
     * gets the item from the specific hand
     * bypasses spigots api to just change the server state without notifying clients
     *
     * @param player Player to get the item from
     * @param hand   Hand to get
     * @return hand ItemStack
     */
    @Nullable
    Object getHandItemInternal(Player player, VRBodyPart hand);

    /**
     * sets the item for the specific hand
     * bypasses spigots api to just change the server state without notifying clients
     *
     * @param player    Player to set the item from
     * @param hand      Hand to set
     * @param itemStack ItemStack to set
     */
    void setHandItemInternal(Player player, VRBodyPart hand, @Nullable Object itemStack);

    /**
     * removes the old attributes and applies the new attributes, for the main hand equipment slot
     */
    void applyEquipmentChange(Player player, Object oldItemStack, Object newItemStack);

    /**
     * makes a copy of the given itemstack
     */
    Object getItemStackCopy(Object itemStack);

    /**
     * checks if the NMS ItemStacks match
     */
    boolean itemStackMatch(Object nmsStack1, Object nmsStack2);

    /**
     * checks if the player is within the reduced vr attack range of the entity
     *
     * @param player the player that is attacked
     * @param entity the entity that attacked
     * @return true if the player is in the reduced attack range, or also true, if the given entity has no attack range
     */
    boolean inReducedAttackRange(Player player, Entity entity);
}
