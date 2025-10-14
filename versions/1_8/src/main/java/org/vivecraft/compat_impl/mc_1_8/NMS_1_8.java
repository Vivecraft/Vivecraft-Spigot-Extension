package org.vivecraft.compat_impl.mc_1_8;

import io.netty.channel.Channel;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.accessors.*;
import org.vivecraft.api.data.VRBodyPart;
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

    protected ReflectionField ServerPlayer_gameMode;
    protected ReflectionField ServerPlayerGameMode_destroyProgressStart;
    protected ReflectionField ServerboundPlayerActionPacket_action;
    protected ReflectionField ServerboundPlayerActionPacketAction_STOP_DESTROY_BLOCK;

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

    protected ReflectionMethod ServerboundUseItemPacket_getFace;
    protected ReflectionField ServerboundUseItemPacket_BlockPos;
    protected ReflectionMethod Direction_from3DDataValue;

    protected ReflectionMethod BlockState_getBlock;
    protected ReflectionMethod Level_getBlockState;
    protected ReflectionField Direction_normal;
    protected ReflectionField Vec3i_x;
    protected ReflectionField Vec3i_y;
    protected ReflectionField Vec3i_z;
    protected Class<?> FenceGateBlock;

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
    protected ReflectionMethod ItemStack_copy;
    protected ReflectionMethod ItemStack_matches;
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

    protected ReflectionField Player_inventory;
    protected ReflectionField Inventory_items;
    protected ReflectionField Inventory_selected;

    protected ReflectionMethod ItemStack_getAttributeModifiers;
    protected ReflectionMethod LivingEntity_getAttributes;
    protected ReflectionMethod AttributeMap_addAttributeModifiers;
    protected ReflectionMethod AttributeMap_removeAttributeModifiers;

    protected Class<?> Mob;
    protected Class<?> MeleeAttackGoal;
    protected ReflectionMethod MeleeAttackGoal_getAttackReachSqr;
    protected ReflectionMethod Entity_distanceToSqr;

    public NMS_1_8() {
        this.init();
        this.initVec3();
        this.initAimFix();
        this.initArmor();
        this.initInventory();
        this.initDualWielding();
        this.initReducedAttack();
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

        this.ServerPlayer_gameMode = ReflectionField.getField(ServerPlayerMapping.FIELD_GAME_MODE);
        this.ServerPlayerGameMode_destroyProgressStart = ReflectionField.getField(
            ServerPlayerGameModeMapping.FIELD_DESTROY_PROGRESS_START);

        this.ServerboundPlayerActionPacket_action = ReflectionField.getField(
            ServerboundPlayerActionPacketMapping.FIELD_ACTION);
        this.ServerboundPlayerActionPacketAction_STOP_DESTROY_BLOCK = ReflectionField.getField(
            ServerboundPlayerActionPacket$ActionMapping.FIELD_STOP_DESTROY_BLOCK);
        this.ItemStack_copy = ReflectionMethod.getMethod(ItemStackMapping.METHOD_COPY);
        this.ItemStack_matches = ReflectionMethod.getMethod(ItemStackMapping.METHOD_MATCHES);
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
        initBlockHit();
    }

    protected void initServer() {
        this.ServerLevel_getServer = ReflectionMethod.getMethod(ServerLevelMapping.METHOD_GET_SERVER);
    }

    protected void initPosition() {
        this.Entity_x = ReflectionField.getField(EntityMapping.FIELD_LOC_X);
        this.Entity_y = ReflectionField.getField(EntityMapping.FIELD_LOC_Y);
        this.Entity_z = ReflectionField.getField(EntityMapping.FIELD_LOC_Z);
    }

    protected void initBlockHit() {
        this.initUseItemOnPacketAccess();
        this.BlockState_getBlock = ReflectionMethod.getMethod(BlockBehaviour$BlockStateBaseMapping.METHOD_GET_BLOCK,
            BlockStateMapping.METHOD_GET_BLOCK);
        this.Level_getBlockState = ReflectionMethod.getMethod(LevelMapping.METHOD_GET_BLOCK_STATE);
        this.Direction_normal = ReflectionField.getField(DirectionMapping.FIELD_NORMAL);
        this.Vec3i_x = ReflectionField.getField(Vec3iMapping.FIELD_X);
        this.Vec3i_y = ReflectionField.getField(Vec3iMapping.FIELD_Y);
        this.Vec3i_z = ReflectionField.getField(Vec3iMapping.FIELD_Z);
        this.FenceGateBlock = ClassGetter.getClass(true, FenceGateBlockMapping.MAPPING);
    }

    protected void initUseItemOnPacketAccess() {
        this.ServerboundUseItemPacket_getFace = ReflectionMethod.getMethod(
            ServerboundUseItemPacketMapping.METHOD_GET_FACE);
        this.ServerboundUseItemPacket_BlockPos = ReflectionField.getField(
            ServerboundUseItemPacketMapping.FIELD_FIELD_179725_B);
        this.Direction_from3DDataValue = ReflectionMethod.getMethod(DirectionMapping.METHOD_FROM3DDATA_VALUE);
    }

    protected void initArmor() {
        this.ItemStack_getItem = ReflectionMethod.getMethod(ItemStackMapping.METHOD_GET_ITEM);
        this.ArmorItem_defense = ReflectionField.getField(ArmorItemMapping.FIELD_DEFENSE);
        this.ArmorItem = ClassGetter.getClass(true, ArmorItemMapping.MAPPING);
    }

    protected void initInventory() {
        this.Player_inventory = ReflectionField.getField(PlayerMapping.FIELD_INVENTORY);
        this.Inventory_items = ReflectionField.getField(InventoryMapping.FIELD_ITEMS, InventoryMapping.FIELD_ITEMS_1);
        this.Inventory_selected = ReflectionField.getField(InventoryMapping.FIELD_SELECTED);
    }

    protected void initDualWielding() {
        this.LivingEntity_getAttributes = ReflectionMethod.getMethod(LivingEntityMapping.METHOD_GET_ATTRIBUTES);
        this.ItemStack_getAttributeModifiers = ReflectionMethod.getMethod(
            ItemStackMapping.METHOD_GET_ATTRIBUTE_MODIFIERS,
            ItemStackMapping.METHOD_FUNC_111283_C);
        this.AttributeMap_addAttributeModifiers = ReflectionMethod.getMethod(
            AttributeMapMapping.METHOD_ADD_TRANSIENT_ATTRIBUTE_MODIFIERS,
            AttributeMapMapping.METHOD_ADD_ATTRIBUTE_MODIFIERS);
        this.AttributeMap_removeAttributeModifiers = ReflectionMethod.getMethod(
            AttributeMapMapping.METHOD_REMOVE_ATTRIBUTE_MODIFIERS);
    }

    protected void initReducedAttack() {
        this.Mob = ClassGetter.getClass(true, MobMapping.MAPPING);
        this.MeleeAttackGoal = ClassGetter.getClass(true, MeleeAttackGoalMapping.MAPPING);
        this.MeleeAttackGoal_getAttackReachSqr = ReflectionMethod.getMethod(
            MeleeAttackGoalMapping.METHOD_GET_ATTACK_REACH_SQR);
        this.Entity_distanceToSqr = ReflectionMethod.getMethod(EntityMapping.METHOD_DISTANCE_TO_SQR);
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
    public Object newVec3(double x, double y, double z) {
        return this.Vec3.newInstance(x, y, z);
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
    public void handlePacketTask(Object packetListener, Runnable task, Object player) {
        this.Server_runOnMainThread.invoke(getServer(player), task);
    }

    @Override
    public void handlePacket(Object player, Object packet, Object packetListener, float xRot, float yRot) {
        if (this.ServerboundPlayerActionPacket.isInstance(packet) &&
            this.ServerboundPlayerActionPacket_action.get(packet) ==
                this.ServerboundPlayerActionPacketAction_STOP_DESTROY_BLOCK.get() &&
            ViveMain.CONFIG.allowFasterBlockBreaking.get())
        {
            // set to 0 to make the game think the block has been breaking for a long time
            this.ServerPlayerGameMode_destroyProgressStart.set(this.ServerPlayer_gameMode.get(player), 0);
        }
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
    public void setSwimPose(Player player) {
        throw new UnsupportedOperationException("No Pose Support");
    }

    @Override
    public void addCrawlPoseWrapper(Player player) {
        throw new UnsupportedOperationException("No Pose Support");
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

    @Nullable
    protected Object getGoalOfClass(Object mob, Class<?> goalClass) {
        Object selector = this.Mob_goalSelector.get(mob);
        Collection<?> goals = (Collection<?>) this.GoalSelector_availableGoals.get(selector);
        for (Object wrappedGoal : goals) {
            Object goal = this.WrappedGoal_goal.get(wrappedGoal);
            if (goalClass.isInstance(goal)) {
                return goal;
            }
        }
        return null;
    }

    @Override
    public Object getHandItemInternal(Player player, VRBodyPart hand) {
        if (hand == VRBodyPart.MAIN_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            return ((Object[]) this.Inventory_items.get(inventory))[(int) this.Inventory_selected.get(inventory)];
        }
        return null;
    }

    @Override
    public void setHandItemInternal(Player player, VRBodyPart hand, Object itemStack) {
        if (hand == VRBodyPart.MAIN_HAND) {
            Object inventory = this.Player_inventory.get(BukkitReflector.getEntityHandle(player));
            ((Object[]) this.Inventory_items.get(inventory))[(int) this.Inventory_selected.get(inventory)] = itemStack;
        }
    }

    @Override
    public void applyEquipmentChange(Player player, Object oldItemStack, Object newItemStack) {
        if (!(boolean) this.ItemStack_matches.invokes(oldItemStack, newItemStack)) {
            Object attributes = this.LivingEntity_getAttributes.invoke(BukkitReflector.getEntityHandle(player));
            if (oldItemStack != null) {
                this.AttributeMap_removeAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(oldItemStack));
            }
            if (newItemStack != null) {
                this.AttributeMap_addAttributeModifiers.invoke(attributes,
                    this.ItemStack_getAttributeModifiers.invoke(newItemStack));
            }
        }
    }

    @Override
    public Object getItemStackCopy(Object itemStack) {
        if (itemStack == null) {
            return null;
        } else {
            return this.ItemStack_copy.invoke(itemStack);
        }
    }

    @Override
    public boolean itemStackMatch(Object nmsStack1, Object nmsStack2) {
        return (boolean) this.ItemStack_matches.invokes(nmsStack1, nmsStack2);
    }

    @Override
    public boolean inReducedAttackRange(Player player, Entity entity) {
        Object nmsEntity = BukkitReflector.getEntityHandle(entity);
        if (!this.Mob.isInstance(nmsEntity)) {
            // no attack range
            return true;
        }

        Object nmsPlayer = BukkitReflector.getEntityHandle(player);

        double attackRangeSqr = this.getAttackReachSqr(nmsEntity, nmsPlayer);
        if (attackRangeSqr < 0) {
            // no melee attacks
            return true;
        }
        // since this is a square, we need to do some calc to add/subtract the regular distance
        attackRangeSqr =
            attackRangeSqr + 2 * Math.sqrt(attackRangeSqr) * ViveMain.CONFIG.mobAttackRangeAdjustment.get() +
                ViveMain.CONFIG.mobAttackRangeAdjustment.get() * ViveMain.CONFIG.mobAttackRangeAdjustment.get();
        return attackRangeSqr > this.getAttackDistanceSqr(nmsEntity, nmsPlayer) ||
            // if they stop let the attack through, or they will stand there forever
            entity.getVelocity().lengthSquared() <= 0.01;
    }

    /**
     * the reach the mob has to attack
     */
    protected double getAttackReachSqr(Object mob, Object target) {
        Object attackGoal = getGoalOfClass(mob, this.MeleeAttackGoal);
        if (attackGoal == null) {
            // no melee attacks
            return -1;
        }
        return (double) this.MeleeAttackGoal_getAttackReachSqr.invoke(attackGoal, target);
    }

    /**
     * the distance between  the mob and the target, to check if it is in attack reach
     */
    protected double getAttackDistanceSqr(Object mob, Object target) {
        return (double) this.Entity_distanceToSqr.invoke(mob, target);
    }

    public Vector3fc getHitDirIfGate(Object player, Object packet) {
        if (isInteractPacket(packet)) {
            Object blockPos = getUseItemOnPos(packet);
            Object block = this.BlockState_getBlock.invoke(
                this.Level_getBlockState.invoke(this.Entity_getLevel.invoke(player), blockPos));
            if (this.FenceGateBlock.isInstance(block)) {
                Object dir = this.Direction_normal.get(getUseItemOnDir(packet));
                if ((int) this.Vec3i_y.get(dir) == 0) {
                    return new Vector3f(-(int) this.Vec3i_x.get(dir),
                        -(int) this.Vec3i_y.get(dir),
                        -(int) this.Vec3i_z.get(dir));
                }
            }
        }
        return null;
    }

    protected boolean isInteractPacket(Object packet) {
        return this.ServerboundUseItemPacket.isInstance(packet);
    }

    protected Object getUseItemOnDir(Object packet) {
        return this.Direction_from3DDataValue.invokes(this.ServerboundUseItemPacket_getFace.invoke(packet));
    }

    protected Object getUseItemOnPos(Object packet) {
        return this.ServerboundUseItemPacket_BlockPos.get(packet);
    }
}
