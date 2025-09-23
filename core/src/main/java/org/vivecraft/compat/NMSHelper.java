package org.vivecraft.compat;

import io.netty.channel.Channel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
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
    void handlePacket(Object packet, Object packetListener, float xRot, float yRot);

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
     * returns if the creeper should use the VR check, instead of the vanilla one
     */
    boolean creeperShouldDoVrCheck(Object creeper);

    /**
     * returns if the creeper should charge
     */
    boolean creeperVrCheck(Object creeper);

    /**
     * Modifies the given entity to be VR compatible
     *
     * @param entity to modify
     */
    void modifyEntity(Entity entity);
}
