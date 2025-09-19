package org.vivecraft.compat_impl.mc_1_8;

import io.netty.channel.Channel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.data.PlayerState;
import org.vivecraft.util.MathUtils;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_8 implements NMSHelper {

    protected ReflectionField LivingEntity_BodyYaw;

    protected ReflectionMethod Entity_getViewVector;
    protected ReflectionField Vec3_X;
    protected ReflectionField Vec3_Y;
    protected ReflectionField Vec3_Z;

    protected ReflectionField Entity_fallDistance;
    protected ReflectionField ServerPlayer_packetListener;
    protected ReflectionField ServerGamePacketListenerImpl_aboveGroundTicks;

    protected ReflectionField ServerCommonPacketListenerImpl_connection;
    protected ReflectionField ServerGamePacketListenerImpl_player;
    protected ReflectionField Connection_channel;
    protected ReflectionMethod Connection_getPacketListener;
    protected ReflectionMethod Connection_isConnected;
    protected ReflectionMethod Packet_handle;
    protected ReflectionMethod Entity_getLevel;
    protected ReflectionMethod ServerLevel_getServer;
    protected ReflectionMethod Server_runOnMainThread;

    protected Class<?> ServerboundUseItemPacket;
    protected Class<?> ServerboundUseItemOnPacket;
    protected Class<?> ServerboundPlayerActionPacket;

    protected ReflectionField Entity_x;
    protected ReflectionField Entity_y;
    protected ReflectionField Entity_z;
    protected ReflectionField Entity_xo;
    protected ReflectionField Entity_yo;
    protected ReflectionField Entity_zo;
    protected ReflectionField Entity_xRot;
    protected ReflectionField Entity_yRot;
    protected ReflectionField Entity_xRotO;
    protected ReflectionField Entity_yRotO;
    protected ReflectionField Entity_eyeHeight;
    protected ReflectionField LivingEntity_yHeadRot;
    protected ReflectionField LivingEntity_yHeadRotO;

    protected ReflectionMethod ItemStack_getItem;
    protected ReflectionField ArmorItem_defense;
    protected Class<?> ArmorItem;

    public NMS_1_8() {
        this.init();
        this.initVec3();
        this.initAimFix();
        this.initArmor();
    }

    protected void init() {
        this.LivingEntity_BodyYaw = ReflectionField.getField(LivingEntityMapping.FIELD_Y_BODY_ROT);

        this.Entity_fallDistance = ReflectionField.getField(EntityMapping.FIELD_FALL_DISTANCE,
            EntityMapping.FIELD_FALL_DISTANCE_1);
        this.ServerPlayer_packetListener = ReflectionField.getField(ServerPlayerMapping.FIELD_CONNECTION);
        this.ServerGamePacketListenerImpl_aboveGroundTicks = ReflectionField.getField(
            ServerGamePacketListenerImplMapping.FIELD_ABOVE_GROUND_TICK_COUNT);
    }

    protected void initVec3() {
        this.Entity_getViewVector = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_LOOK_ANGLE);
        this.Vec3_X = ReflectionField.getField(Vec3Mapping.FIELD_X);
        this.Vec3_Y = ReflectionField.getField(Vec3Mapping.FIELD_Y);
        this.Vec3_Z = ReflectionField.getField(Vec3Mapping.FIELD_Z);
    }

    protected void initAimFix() {
        this.ServerCommonPacketListenerImpl_connection = ReflectionField.getField(
            ServerCommonPacketListenerImplMapping.FIELD_CONNECTION,
            ServerGamePacketListenerImplMapping.FIELD_CONNECTION);
        this.ServerGamePacketListenerImpl_player = ReflectionField.getField(
            ServerGamePacketListenerImplMapping.FIELD_PLAYER);
        this.Connection_channel = ReflectionField.getField(ConnectionMapping.FIELD_CHANNEL);
        this.Connection_getPacketListener = ReflectionMethod.getMethod(ConnectionMapping.METHOD_GET_PACKET_LISTENER);
        this.Connection_isConnected = ReflectionMethod.getMethod(ConnectionMapping.METHOD_IS_CONNECTED);
        this.Packet_handle = ReflectionMethod.getMethod(PacketMapping.METHOD_HANDLE);

        this.ServerboundUseItemPacket = ClassGetter.getClass(false, ServerboundUseItemPacketMapping.MAPPING);
        this.ServerboundUseItemOnPacket = ClassGetter.getClass(false, ServerboundUseItemOnPacketMapping.MAPPING);
        this.ServerboundPlayerActionPacket = ClassGetter.getClass(false, ServerboundPlayerActionPacketMapping.MAPPING);

        this.Entity_xo = ReflectionField.getField(EntityMapping.FIELD_XO);
        this.Entity_yo = ReflectionField.getField(EntityMapping.FIELD_YO);
        this.Entity_zo = ReflectionField.getField(EntityMapping.FIELD_ZO);
        this.Entity_xRotO = ReflectionField.getField(EntityMapping.FIELD_X_ROT_O);
        this.Entity_yRotO = ReflectionField.getField(EntityMapping.FIELD_Y_ROT_O);
        this.Entity_eyeHeight = ReflectionField.getField(EntityMapping.FIELD_LENGTH, EntityMapping.FIELD_EYE_HEIGHT);
        this.LivingEntity_yHeadRot = ReflectionField.getField(LivingEntityMapping.FIELD_Y_HEAD_ROT);
        this.LivingEntity_yHeadRotO = ReflectionField.getField(LivingEntityMapping.FIELD_Y_HEAD_ROT_O);

        this.Server_runOnMainThread = ReflectionMethod.getMethod(
            BlockableEventLoopMapping.METHOD_EXECUTE_IF_POSSIBLE,
            BlockableEventLoopMapping.METHOD_EXECUTE,
            BlockableEventLoopMapping.METHOD_POST_TO_MAIN_THREAD);
        initPosition();
        initRotation();
        initServer();
    }

    protected void initServer() {
        this.Entity_getLevel = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_COMMAND_SENDER_WORLD);
        this.ServerLevel_getServer = ReflectionMethod.getMethod(ServerLevelMapping.METHOD_GET_SERVER);
    }

    protected void initPosition() {
        this.Entity_x = ReflectionField.getField(EntityMapping.FIELD_LOC_X);
        this.Entity_y = ReflectionField.getField(EntityMapping.FIELD_LOC_Y);
        this.Entity_z = ReflectionField.getField(EntityMapping.FIELD_LOC_Z);
    }

    protected void initArmor() {
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
        this.ArmorItem_defense = ReflectionField.getField(ArmorItemMapping.FIELD_DEFENSE);
        this.ArmorItem = ClassGetter.getClass(true, ArmorItemMapping.MAPPING);
    }

    protected Vector getPosition(Object serverPlayer) {
        return new Vector(
            (double) this.Entity_x.get(serverPlayer),
            (double) this.Entity_y.get(serverPlayer),
            (double) this.Entity_z.get(serverPlayer));
    }

    protected void setPosition(Object serverPlayer, double x, double y, double z) {
        this.Entity_x.set(serverPlayer, x);
        this.Entity_y.set(serverPlayer, y);
        this.Entity_z.set(serverPlayer, z);
    }

    protected void initRotation() {
        this.Entity_xRot = ReflectionField.getField(EntityMapping.FIELD_X_ROT);
        this.Entity_yRot = ReflectionField.getField(EntityMapping.FIELD_Y_ROT);
    }

    protected Vector getRotation(Object serverPlayer) {
        return new Vector(
            (float) this.Entity_xRot.get(serverPlayer),
            (float) this.Entity_yRot.get(serverPlayer), 0);
    }

    protected void setRotation(Object serverPlayer, float xRot, float yRot) {
        this.Entity_xRot.set(serverPlayer, xRot);
        this.Entity_yRot.set(serverPlayer, yRot);
    }

    @Override
    public float getLivingEntityBodyYaw(LivingEntity entity) {
        return (float) this.LivingEntity_BodyYaw.get(BukkitReflector.getHandle(entity));
    }

    @Override
    public Vector3f getViewVector(Entity entity) {
        Object vec3 = this.Entity_getViewVector.invoke(BukkitReflector.getHandle(entity));
        return new Vector3f(
            (float) (double) this.Vec3_X.get(vec3),
            (float) (double) this.Vec3_Y.get(vec3),
            (float) (double) this.Vec3_Z.get(vec3));
    }

    @Override
    public ItemStack setItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(fallback);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void resetFallDistance(Player player) {
        this.Entity_fallDistance.set(BukkitReflector.getHandle(player), 0);
        this.ServerGamePacketListenerImpl_aboveGroundTicks.set(
            this.ServerPlayer_packetListener.get(BukkitReflector.getHandle(player)), 0);
    }

    @Override
    public Object getConnection(Player player) {
        return this.ServerCommonPacketListenerImpl_connection.get(
            this.ServerPlayer_packetListener.get(BukkitReflector.getHandle(player)));
    }

    @Override
    public boolean isConnectionConnected(Object connection) {
        return (boolean) this.Connection_isConnected.invoke(connection);
    }

    @Override
    public Channel getChannel(Object connection) {
        return (Channel) this.Connection_channel.get(connection);
    }

    @Override
    public Object getPacketListener(Object connection) {
        return this.Connection_getPacketListener.invoke(connection);
    }

    @Override
    public Object getPlayer(Object packetListener) {
        return this.ServerGamePacketListenerImpl_player.get(packetListener);
    }

    @Override
    public Object getServer(Object serverPlayer) {
        return this.ServerLevel_getServer.invoke(this.Entity_getLevel.invoke(serverPlayer));
    }

    @Override
    public void runOnMainThread(Object server, Runnable runnable) {
        this.Server_runOnMainThread.invoke(server, runnable);
    }

    @Override
    public void handlePacket(Object packet, Object packetListener, float xRot, float yRot) {
        this.Packet_handle.invoke(packet, packetListener);
    }

    @Override
    public boolean needsAimfixHandling(Object packet) {
        return (this.ServerboundUseItemPacket != null && this.ServerboundUseItemPacket.isInstance(packet)) ||
            (this.ServerboundUseItemOnPacket != null && this.ServerboundUseItemOnPacket.isInstance(packet)) ||
            (this.ServerboundPlayerActionPacket != null && this.ServerboundPlayerActionPacket.isInstance(packet));
    }

    @Override
    public PlayerState getPlayerState(Object serverPlayer) {
        Vector pos = this.getPosition(serverPlayer);
        Vector rot = this.getRotation(serverPlayer);
        return new PlayerState(pos.getX(), pos.getY(), pos.getZ(), (double) this.Entity_xo.get(serverPlayer),
            (double) this.Entity_yo.get(serverPlayer), (double) this.Entity_zo.get(serverPlayer),
            (float) rot.getX(), (float) rot.getY(),
            (float) this.Entity_xRotO.get(serverPlayer), (float) this.Entity_yRotO.get(serverPlayer),
            (float) this.LivingEntity_yHeadRot.get(serverPlayer), (float) this.LivingEntity_yHeadRotO.get(serverPlayer),
            (float) this.Entity_eyeHeight.get(serverPlayer));
    }

    @Override
    public void setPlayerState(Object serverPlayer, Vector position, float xRot, float yRot) {
        this.setPosition(serverPlayer, position.getX(), position.getY(), position.getZ());
        this.Entity_xo.set(serverPlayer, position.getX());
        this.Entity_yo.set(serverPlayer, position.getY());
        this.Entity_zo.set(serverPlayer, position.getZ());
        this.setRotation(serverPlayer, xRot, yRot);
        this.Entity_xRotO.set(serverPlayer, xRot);
        this.Entity_yRotO.set(serverPlayer, yRot);
        this.LivingEntity_yHeadRot.set(serverPlayer, yRot);
        this.LivingEntity_yHeadRotO.set(serverPlayer, yRot);
        // non 0 to avoid divisions by 0
        this.Entity_eyeHeight.set(serverPlayer, 0.0001F);
    }

    @Override
    public boolean restorePlayerState(Object serverPlayer, PlayerState original, Vector modifiedPosition) {
        this.Entity_xo.set(serverPlayer, original.prevX);
        this.Entity_yo.set(serverPlayer, original.prevY);
        this.Entity_zo.set(serverPlayer, original.prevZ);
        this.setRotation(serverPlayer, original.xRot, original.yRot);
        this.Entity_xRotO.set(serverPlayer, original.prevXRot);
        this.Entity_yRotO.set(serverPlayer, original.prevYRot);
        this.LivingEntity_yHeadRot.set(serverPlayer, original.yHeadRot);
        this.LivingEntity_yHeadRotO.set(serverPlayer, original.prevYHeadRot);
        this.Entity_eyeHeight.set(serverPlayer, original.eyeHeight);

        Vector newPos = this.getPosition(serverPlayer);
        double x = newPos.getX();
        double y = newPos.getY();
        double z = newPos.getZ();

        // if the player position changed, use that
        if ((modifiedPosition != null &&
            !MathUtils.equalsPosition(x, y, z, modifiedPosition.getX(), modifiedPosition.getY(),
                modifiedPosition.getZ())
        ) || (modifiedPosition == null && !MathUtils.equalsPosition(x, y, z, original.x, original.y, original.z)))
        {
            this.setPosition(serverPlayer, x, y, z);
            return true;
        } else {
            this.setPosition(serverPlayer, original.x, original.y, original.z);
            return false;
        }
    }

    @Override
    public double getArmorValue(ItemStack itemStack) {
        Object item = this.ItemStack_getItem.invoke(BukkitReflector.asNMSCopy(itemStack));
        if (this.ArmorItem.isInstance(item)) {
            return (int) this.ArmorItem_defense.get(item);
        } else {
            return 0;
        }
    }
}
