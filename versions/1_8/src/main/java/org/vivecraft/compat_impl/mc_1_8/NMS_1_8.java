package org.vivecraft.compat_impl.mc_1_8;

import io.netty.channel.Channel;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.data.PlayerState;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.MathUtils;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class NMS_1_8 implements NMSHelper {

    protected ReflectionField LivingEntity_BodyYaw;
    protected ReflectionMethod Entity_getUUID;

    protected ReflectionMethod Entity_getViewVector;
    protected ReflectionField Vec3_X;
    protected ReflectionField Vec3_Y;
    protected ReflectionField Vec3_Z;
    protected ReflectionConstructor Vec3;

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
    protected ReflectionMethod Entity_getEyeHeight;
    protected ReflectionField LivingEntity_yHeadRot;
    protected ReflectionField LivingEntity_yHeadRotO;

    protected ReflectionMethod ItemStack_getItem;
    protected ReflectionField ArmorItem_defense;
    protected Class<?> ArmorItem;

    protected ReflectionMethod Level_Clip;

    protected ReflectionMethod Mob_getTarget;
    protected ReflectionField Mob_goalSelector;
    protected ReflectionField Mob_targetSelector;
    protected ReflectionField GoalSelector_availableGoals;
    protected ReflectionMethod GoalSelector_addGoal;
    protected ReflectionMethod GoalSelector_removeGoal;
    protected ReflectionField WrappedGoal_goal;
    protected ReflectionField WrappedGoal_priority;

    public NMS_1_8() {
        this.init();
        this.initVec3();
        this.initAimFix();
        this.initArmor();
    }

    protected void init() {
        this.LivingEntity_BodyYaw = ReflectionField.getField(LivingEntityMapping.FIELD_Y_BODY_ROT);
        this.Entity_getUUID = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_UUID);

        this.Entity_fallDistance = ReflectionField.getField(EntityMapping.FIELD_FALL_DISTANCE,
            EntityMapping.FIELD_FALL_DISTANCE_1);
        this.ServerPlayer_packetListener = ReflectionField.getField(ServerPlayerMapping.FIELD_CONNECTION);
        this.ServerGamePacketListenerImpl_aboveGroundTicks = ReflectionField.getField(
            ServerGamePacketListenerImplMapping.FIELD_ABOVE_GROUND_TICK_COUNT);

        this.Level_Clip = ReflectionMethod.getMethod(BlockGetterMapping.METHOD_CLIP,
            LevelMapping.METHOD_FUNC_200259_A, LevelMapping.METHOD_FUNC_147447_A);

        this.Entity_getEyeHeight = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_EYE_HEIGHT);

        this.Mob_getTarget = ReflectionMethod.getMethod(MobMapping.METHOD_GET_TARGET);
        this.Mob_goalSelector = ReflectionField.getField(MobMapping.FIELD_GOAL_SELECTOR);
        this.Mob_targetSelector = ReflectionField.getField(MobMapping.FIELD_TARGET_SELECTOR);
        this.GoalSelector_availableGoals = ReflectionField.getField(GoalSelectorMapping.FIELD_AVAILABLE_GOALS,
            GoalSelectorMapping.FIELD_FIELD_75782_A, GoalSelectorMapping.FIELD_FIELD_75782_A_1);
        this.GoalSelector_addGoal = ReflectionMethod.getMethod(GoalSelectorMapping.METHOD_ADD_GOAL);
        this.GoalSelector_removeGoal = ReflectionMethod.getMethod(GoalSelectorMapping.METHOD_REMOVE_GOAL);
        this.WrappedGoal_goal = ReflectionField.getField(WrappedGoalMapping.FIELD_GOAL,
            EntityAITasks$EntityAITaskEntryMapping.FIELD_FIELD_75733_A);
        this.WrappedGoal_priority = ReflectionField.getField(WrappedGoalMapping.FIELD_PRIORITY,
            EntityAITasks$EntityAITaskEntryMapping.FIELD_FIELD_75731_B);
    }

    protected void initVec3() {
        this.Entity_getViewVector = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_LOOK_ANGLE);
        this.Vec3_X = ReflectionField.getField(Vec3Mapping.FIELD_X);
        this.Vec3_Y = ReflectionField.getField(Vec3Mapping.FIELD_Y);
        this.Vec3_Z = ReflectionField.getField(Vec3Mapping.FIELD_Z);
        this.Vec3 = ReflectionConstructor.getConstructor(Vec3Mapping.CONSTRUCTOR_0);
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

        this.Entity_getLevel = ReflectionMethod.getMethod(EntityMapping.METHOD_LEVEL,
            EntityMapping.METHOD_GET_COMMAND_SENDER_WORLD);
        initPosition();
        initRotation();
        initServer();
    }

    protected void initServer() {
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

    @Override
    public Vector getEntityPosition(Object nmsEntity) {
        return new Vector(
            (double) this.Entity_x.get(nmsEntity),
            (double) this.Entity_y.get(nmsEntity),
            (double) this.Entity_z.get(nmsEntity));
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
        return (float) this.LivingEntity_BodyYaw.get(BukkitReflector.getEntityHandle(entity));
    }

    @Override
    public Vector3f getViewVector(Entity entity) {
        Object vec3 = this.Entity_getViewVector.invoke(BukkitReflector.getEntityHandle(entity));
        return new Vector3f(
            (float) (double) this.Vec3_X.get(vec3),
            (float) (double) this.Vec3_Y.get(vec3),
            (float) (double) this.Vec3_Z.get(vec3));
    }

    @Override
    public Vector vec3ToVector(Object vec3) {
        return new Vector(
            (double) this.Vec3_X.get(vec3),
            (double) this.Vec3_Y.get(vec3),
            (double) this.Vec3_Z.get(vec3));
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
        this.Entity_fallDistance.set(BukkitReflector.getEntityHandle(player), 0);
        this.ServerGamePacketListenerImpl_aboveGroundTicks.set(
            this.ServerPlayer_packetListener.get(BukkitReflector.getEntityHandle(player)), 0);
    }

    @Override
    public Object getConnection(Player player) {
        return this.ServerCommonPacketListenerImpl_connection.get(
            this.ServerPlayer_packetListener.get(BukkitReflector.getEntityHandle(player)));
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
    public Object getLevel(Object entity) {
        return this.Entity_getLevel.invoke(entity);
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
        Vector pos = this.getEntityPosition(serverPlayer);
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

        Vector newPos = this.getEntityPosition(serverPlayer);
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

    @Override
    public boolean isVRPlayer(Object nmsEntity) {
        return nmsEntity != null && ViveMain.isVRPlayer((UUID) this.Entity_getUUID.invoke(nmsEntity));
    }

    @Override
    public boolean isTargetVrPlayer(Object mob) {
        Object target = this.Mob_getTarget.invoke(mob);
        return target != null && isVRPlayer(target);
    }

    @Override
    public VivePlayer getVRPlayer(Object nmsEntity) {
        return ViveMain.getVivePlayer((UUID) this.Entity_getUUID.invoke(nmsEntity));
    }

    @Override
    public Vector getHeadPosVR(Object nmsEntity) {
        if (nmsEntity == null) {
            return null;
        } else if (isVRPlayer(nmsEntity)) {
            VivePlayer vive = getVRPlayer(nmsEntity);
            return vive.getHMDPos();
        } else {
            return getEyePosition(nmsEntity);
        }
    }

    protected Vector getEyePosition(Object nmsEntity) {
        return new Vector(
            (double) this.Entity_xo.get(nmsEntity),
            (double) this.Entity_yo.get(nmsEntity) + (float) this.Entity_getEyeHeight.invoke(nmsEntity),
            (double) this.Entity_zo.get(nmsEntity));
    }

    @Override
    public Vector getViewVectorVR(Object nmsEntity) {
        if (nmsEntity == null) {
            return null;
        } else if (isVRPlayer(nmsEntity)) {
            VivePlayer vive = getVRPlayer(nmsEntity);
            return MathUtils.toBukkitVec(vive.getHMDDir());
        } else {
            return vec3ToVector(this.Entity_getViewVector.invoke(nmsEntity));
        }
    }

    @Override
    public boolean clipWorld(
        Object level, Vector from, Vector to, BlockContext block, FluidContext fluid, Object sourceEntity)
    {
        return this.Level_Clip.invoke(level,
            this.Vec3.newInstance(from.getX(), from.getY(), from.getZ()),
            this.Vec3.newInstance(to.getX(), to.getY(), to.getZ()),
            fluid == FluidContext.NONE, block != BlockContext.COLLIDER, false) != null;
    }

    @Override
    public boolean canSeeEachOther(
        Object player, Object target, double tolerance, boolean scaleWithDistance, boolean visualClip,
        double... yValues)
    {
        Vector playerView = ViveMain.NMS.getViewVectorVR(player);
        Vector playerHead = ViveMain.NMS.getHeadPosVR(player);
        Vector targetPos = ViveMain.NMS.getEntityPosition(target);
        if (yValues == null || yValues.length == 0) {
            yValues = new double[]{targetPos.getY() + (float) this.Entity_getEyeHeight.invoke(target)};
        }
        for (double yValue : yValues) {
            Vector targetOffsetPos = new Vector().copy(targetPos);
            targetOffsetPos.setY(yValue);
            Vector playerToTarget = new Vector().copy(targetOffsetPos).subtract(playerHead);
            double dist = scaleWithDistance ? playerToTarget.length() : 1.0;
            playerToTarget.normalize();
            if (playerToTarget.dot(playerView) > 1.0 - tolerance / dist) {
                // looks in the right direction
                if (!ViveMain.NMS.clipWorld(ViveMain.NMS.getLevel(player), playerHead, targetOffsetPos,
                    visualClip ? BlockContext.VISUAL : BlockContext.COLLIDER, FluidContext.NONE, player))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void modifyEntity(Entity entity) {
        if (entity instanceof Creeper) {
            if (!replaceGoal(entity, false, goal -> ViveMain.MC_MODS.creeperHelper().isSwellGoal(goal),
                creeper -> ViveMain.MC_MODS.creeperHelper().getCreeperSwellGoal(creeper)))
            {
                throw new RuntimeException("Could not find swell goal for creeper");
            }
        } else if (entity instanceof Enderman) {
            if (!replaceGoal(entity, true, goal -> ViveMain.MC_MODS.endermanHelper().isLookForPlayerGoal(goal),
                enderman -> ViveMain.MC_MODS.endermanHelper().getEndermanLookForPlayer(enderman)))
            {
                throw new RuntimeException("Could not find lookforplayer goal for enderman");
            }
            Debug.log("look for player replaced");
        }
    }

    protected boolean replaceGoal(
        Entity entity, boolean isTarget, Predicate<Object> isGoal, Function<Object, Object> newGoal)
    {
        Object nmsEntity = BukkitReflector.getEntityHandle(entity);
        Object selector = isTarget ? this.Mob_targetSelector.get(nmsEntity) : this.Mob_goalSelector.get(nmsEntity);
        Collection<?> goals = (Collection<?>) this.GoalSelector_availableGoals.get(selector);
        int priority = Integer.MAX_VALUE;
        for (Object wrappedGoal : goals) {
            Object goal = this.WrappedGoal_goal.get(wrappedGoal);
            if (isGoal.test(goal)) {
                priority = (int) this.WrappedGoal_priority.get(wrappedGoal);
                this.GoalSelector_removeGoal.invoke(selector, goal);
                break;
            }
        }
        if (priority == Integer.MAX_VALUE) {
            for (Object wrappedGoal : goals) {
                Object goal = this.WrappedGoal_goal.get(wrappedGoal);
                ViveMain.LOGGER.info("entity has goal: " + goal.getClass().getName());
            }
            return false;
        }
        this.GoalSelector_addGoal.invoke(selector, priority,
            newGoal.apply(nmsEntity));
        return true;
    }
}
