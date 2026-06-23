package org.vivecraft.compat_impl.mc_26_1;

import io.netty.channel.Channel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.data.PlayerState;
import org.vivecraft.debug.Debug;
import org.vivecraft.util.MathUtils;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class NMS_26_1 implements NMSHelper {

    // reflection needed, because these are private fields/methods
    private final ReflectionField ServerGamePacketListenerImpl_aboveGroundTickCount;
    private final ReflectionField ServerCommonPacketListenerImpl_connection;
    private final ReflectionField Connection_channel;
    private final ReflectionField ServerPlayerGameMode_destroyProgressStart;
    private final ReflectionField Entity_eyeHeight;
    private final ReflectionField SynchedEntityData_itemsById;
    private final ReflectionField Entity_DATA_POSE;
    private final ReflectionField Mob_DEFAULT_ATTACK_REACH;
    private final ReflectionField Mob_goalSelector;
    private final ReflectionField Mob_targetSelector;
    private final ReflectionField ServerboundUseItemPacket_xRot;
    private final ReflectionField ServerboundUseItemPacket_yRot;

    private final ReflectionMethod Entity_removeAfterChangingDimensions;
    private final ReflectionMethod Mob_getAttackBoundingBox;
    private final ReflectionMethod LivingEntity_getHitbox;

    public NMS_26_1() {
        this.ServerGamePacketListenerImpl_aboveGroundTickCount = ReflectionField.getRaw(
            ServerGamePacketListenerImpl.class, "aboveGroundTickCount");
        this.ServerCommonPacketListenerImpl_connection = ReflectionField.getRaw(
            ServerCommonPacketListenerImpl.class, "connection");
        this.Connection_channel = ReflectionField.getRaw(Connection.class, "channel");
        this.ServerPlayerGameMode_destroyProgressStart = ReflectionField.getRaw(
            ServerPlayerGameMode.class, "destroyProgressStart");
        this.Entity_eyeHeight = ReflectionField.getRaw(Entity.class, "eyeHeight");
        this.SynchedEntityData_itemsById = ReflectionField.getRaw(SynchedEntityData.class, "itemsById");
        this.Entity_DATA_POSE = ReflectionField.getRaw(Entity.class, "DATA_POSE");
        this.Mob_DEFAULT_ATTACK_REACH = ReflectionField.getRaw(Mob.class, "DEFAULT_ATTACK_REACH");
        this.Mob_goalSelector = ReflectionField.getRaw(Mob.class, "goalSelector");
        this.Mob_targetSelector = ReflectionField.getRaw(Mob.class, "targetSelector");
        this.ServerboundUseItemPacket_xRot = ReflectionField.getRaw(ServerboundUseItemPacket.class, "xRot");
        this.ServerboundUseItemPacket_yRot = ReflectionField.getRaw(ServerboundUseItemPacket.class, "yRot");

        this.Entity_removeAfterChangingDimensions = ReflectionMethod.getRaw(Entity.class,
            "removeAfterChangingDimensions");
        this.Mob_getAttackBoundingBox = ReflectionMethod.getRaw(Mob.class, "getAttackBoundingBox", double.class);
        this.LivingEntity_getHitbox = ReflectionMethod.getRaw(LivingEntity.class, "getHitbox");
    }

    @Override
    public float getLivingEntityBodyYaw(org.bukkit.entity.LivingEntity entity) {
        return ((LivingEntity) BukkitReflector.getEntityHandle(entity)).yBodyRot;
    }

    @Override
    public Vector3f getViewVector(org.bukkit.entity.Entity entity) {
        Vec3 dir = ((Entity) BukkitReflector.getEntityHandle(entity)).getLookAngle();
        return new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);
    }

    @Override
    public Vector vec3ToVector(Object vec3) {
        return new Vector(((Vec3) vec3).x, ((Vec3) vec3).y, ((Vec3) vec3).z);
    }

    @Override
    public Object newVec3(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    @Override
    public Class<?> getItemstackClass() {
        return ItemStack.class;
    }

    @Override
    public org.bukkit.inventory.ItemStack setItemStackName(
        org.bukkit.inventory.ItemStack itemStack, String translationKey, String fallback)
    {
        ItemStack nmsStack = (ItemStack) BukkitReflector.asNMSCopy(itemStack);
        nmsStack.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(translationKey, fallback));
        return BukkitReflector.asBukkitCopy(nmsStack);
    }

    @Override
    public boolean hasItemStackName(org.bukkit.inventory.ItemStack itemStack, String translationKey, String fallback) {
        ItemStack nmsStack = (ItemStack) BukkitReflector.asNMSCopy(itemStack);
        if (nmsStack.has(DataComponents.CUSTOM_NAME)) {
            Component component = nmsStack.getHoverName();
            if (fallback.equals(component.getString())) {
                return true;
            }
            ComponentContents content = component.getContents();
            return content instanceof TranslatableContents &&
                translationKey.equals(((TranslatableContents) content).getKey());
        }
        return false;
    }

    @Override
    public void resetFallDistance(org.bukkit.entity.Player player) {
        ServerPlayer nmsPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);
        nmsPlayer.fallDistance = 0;
        // need to use reflection for that, because the field is private
        this.ServerGamePacketListenerImpl_aboveGroundTickCount.set(nmsPlayer.connection, 0);
    }

    @Override
    public Object getConnection(org.bukkit.entity.Player player) {
        return this.ServerCommonPacketListenerImpl_connection.get(
            ((ServerPlayer) BukkitReflector.getEntityHandle(player)).connection);
    }

    @Override
    public boolean isConnectionConnected(Object connection) {
        return ((Connection) connection).isConnected();
    }

    @Override
    public Channel getChannel(Object connection) {
        return (Channel) this.Connection_channel.get(connection);
    }

    @Override
    public Object getPacketListener(Object connection) {
        return ((Connection) connection).getPacketListener();
    }

    @Override
    public Object getServer(Object serverPlayer) {
        return ((ServerPlayer) serverPlayer).level().getServer();
    }

    @Override
    public Object getLevel(Object entity) {
        return ((Entity) entity).level();
    }

    @Override
    public void handlePacketTask(Object packetListener, Runnable task, Object player) {
        new WrappedPacket(task, ((ServerPlayer) player).level()).handle((PacketListener) packetListener);
    }

    @Override
    public Object getPlayer(Object packetListener) {
        return ((ServerGamePacketListenerImpl) packetListener).player;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePacket(Object player, Object packet, Object packetListener, float xRot, float yRot) {
        if (packet instanceof ServerboundUseItemPacket) {
            // modify the original packet
            this.ServerboundUseItemPacket_xRot.set(packet, xRot);
            this.ServerboundUseItemPacket_yRot.set(packet, yRot);
        }
        if (packet instanceof ServerboundPlayerActionPacket actionPacket &&
            actionPacket.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK &&
            ViveMain.CONFIG.allowFasterBlockBreaking.get())
        {
            // set to 0 to make the game think the block has been breaking for a long time
            this.ServerPlayerGameMode_destroyProgressStart.set(((ServerPlayer) player).gameMode, 0);
        }
        ((Packet<PacketListener>) packet).handle((PacketListener) packetListener);
    }

    @Override
    public boolean needsAimfixHandling(Object packet) {
        return packet instanceof ServerboundUseItemPacket ||
            packet instanceof ServerboundUseItemOnPacket ||
            packet instanceof ServerboundPlayerActionPacket;
    }

    @Override
    public PlayerState getPlayerState(Object serverPlayer) {
        ServerPlayer player = (ServerPlayer) serverPlayer;
        return new PlayerState(player.getX(), player.getY(), player.getZ(),
            player.xo, player.yo, player.zo,
            player.getXRot(), player.getYRot(),
            player.xRotO, player.yRotO,
            player.yHeadRot, player.yHeadRotO,
            player.getEyeHeight());
    }

    @Override
    public void setPlayerState(Object serverPlayer, Vector position, float xRot, float yRot) {
        ServerPlayer player = (ServerPlayer) serverPlayer;
        player.setPosRaw(position.getX(), position.getY(), position.getZ());
        player.xo = position.getX();
        player.yo = position.getY();
        player.zo = position.getZ();
        player.setXRot(xRot);
        player.setYRot(yRot);
        player.xRotO = xRot;
        player.yRotO = yRot;
        player.yHeadRot = yRot;
        player.yHeadRotO = yRot;
        // non 0 to avoid divisions by 0
        this.Entity_eyeHeight.set(serverPlayer, 0.0001F);
    }

    @Override
    public boolean restorePlayerState(Object serverPlayer, PlayerState original, Vector modifiedPosition) {
        ServerPlayer player = (ServerPlayer) serverPlayer;
        player.xo = original.prevX;
        player.yo = original.prevY;
        player.zo = original.prevZ;
        player.setXRot(original.xRot);
        player.setYRot(original.yRot);
        player.xRotO = original.prevXRot;
        player.yRotO = original.prevYRot;
        player.yHeadRot = original.yHeadRot;
        player.yHeadRotO = original.prevYHeadRot;
        this.Entity_eyeHeight.set(player, original.eyeHeight);

        Vector newPos = this.getEntityPosition(player);
        double x = newPos.getX();
        double y = newPos.getY();
        double z = newPos.getZ();

        // if the player position changed, use that
        if ((modifiedPosition != null &&
            !MathUtils.equalsPosition(x, y, z, modifiedPosition.getX(), modifiedPosition.getY(),
                modifiedPosition.getZ())
        ) || (modifiedPosition == null && !MathUtils.equalsPosition(x, y, z, original.x, original.y, original.z)))
        {
            player.setPosRaw(x, y, z);
            return true;
        } else {
            player.setPosRaw(original.x, original.y, original.z);
            return false;
        }
    }

    @Override
    public double getArmorValue(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsStack = (ItemStack) BukkitReflector.asNMSCopy(itemStack);
        List<ItemAttributeModifiers.Entry> modifiers = nmsStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.EMPTY).modifiers();

        List<AttributeModifier> attributeModifiers = modifiers.stream().filter(
                entry -> entry.attribute().is(Attributes.ARMOR)).map(ItemAttributeModifiers.Entry::modifier)
            .toList();

        return applyAttributeModifiers(0, attributeModifiers);
    }


    protected double applyAttributeModifiers(double original, List<AttributeModifier> modifiers) {
        for (AttributeModifier modifier : modifiers) {
            double amount = modifier.amount();
            Object operation = modifier.operation();
            if (operation == AttributeModifier.Operation.ADD_VALUE) {
                original += amount;
            } else if (operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                original += amount * original;
            }
        }
        return original;
    }

    @Override
    public boolean isVRPlayer(Object nmsEntity) {
        return nmsEntity != null && ViveMain.isVRPlayer(((Entity) nmsEntity).getUUID());
    }

    @Override
    public boolean isTargetVrPlayer(Object mob) {
        LivingEntity target = ((Mob) mob).getTarget();
        return target != null && isVRPlayer(target);
    }

    @Override
    public VivePlayer getVRPlayer(Object nmsEntity) {
        return ViveMain.getVivePlayer(((Entity) nmsEntity).getUUID());
    }

    @Override
    public Vector getHeadPosVR(Object nmsEntity) {
        if (nmsEntity == null) {
            return null;
        } else if (isVRPlayer(nmsEntity)) {
            VivePlayer vive = getVRPlayer(nmsEntity);
            return vive.getHMDPos();
        } else {
            return vec3ToVector(((Entity) nmsEntity).getEyePosition());
        }
    }

    @Override
    public Vector getEntityPosition(Object nmsEntity) {
        return vec3ToVector(((Entity) nmsEntity).position());
    }

    @Override
    public Vector getViewVectorVR(Object nmsEntity) {
        if (nmsEntity == null) {
            return null;
        } else if (isVRPlayer(nmsEntity)) {
            VivePlayer vive = getVRPlayer(nmsEntity);
            return MathUtils.toBukkitVec(vive.getHMDDir());
        } else {
            return vec3ToVector(((Entity) nmsEntity).getLookAngle());
        }
    }

    @Override
    public void modifyEntity(org.bukkit.entity.Entity entity) {
        if (entity instanceof org.bukkit.entity.Enderman) {
            if (!(BukkitReflector.getEntityHandle(entity) instanceof VREnderMan)) {
                Debug.log("replacing Enderman");
                replaceEntity(entity, newVREnderman());
            }
        } else if (entity instanceof org.bukkit.entity.Creaking) {
            if (!(BukkitReflector.getEntityHandle(entity) instanceof VRCreaking)) {
                Debug.log("replacing Creaking");
                replaceEntity(entity, newVRCreaking());
            }
        } else if (entity instanceof org.bukkit.entity.Creeper) {
            if (replaceGoal(entity, false, goal -> ViveMain.MC_MODS.creeperHelper().isSwellGoal(goal),
                creeper -> (Goal) ViveMain.MC_MODS.creeperHelper().getCreeperSwellGoal(creeper), "SwellGoal"))
            {
                Debug.log("SwellGoal replaced");
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Function<Entity, Entity> newVREnderman() {
        return entity -> new VREnderMan((EntityType<EnderMan>) entity.getType(), entity.level());
    }

    @SuppressWarnings("unchecked")
    protected Function<Entity, Entity> newVRCreaking() {
        return entity -> new VRCreaking((EntityType<Creaking>) entity.getType(), entity.level());
    }

    protected boolean replaceGoal(
        org.bukkit.entity.Entity entity, boolean isTarget, Predicate<Object> isGoal, Function<Entity, Goal> newGoal,
        String goalName)
    {
        Mob nmsMob = (Mob) BukkitReflector.getEntityHandle(entity);
        GoalSelector selector = isTarget ?
            (GoalSelector) this.Mob_targetSelector.get(nmsMob) :
            (GoalSelector) this.Mob_goalSelector.get(nmsMob);
        Set<WrappedGoal> goals = selector.getAvailableGoals();
        if (goals.isEmpty()) {
            Debug.log("Couldn't replace goal '%s' for entity %s\n no goals, probably modified by another plugin",
                goalName, nmsMob.getClass().getName());
            return false;
        }
        int priority = Integer.MAX_VALUE;
        boolean removed = false;

        for (WrappedGoal wrappedGoal : goals) {
            Goal goal = wrappedGoal.getGoal();
            if (isGoal.test(goal)) {
                priority = wrappedGoal.getPriority();
                selector.removeGoal(goal);
                removed = true;
                break;
            }
        }
        if (!removed) {
            List<String> gs = new ArrayList<>();
            for (WrappedGoal wrappedGoal : goals) {
                Object goal = wrappedGoal.getGoal();
                gs.add(wrappedGoal.getPriority() + ": " + goal.getClass().getName());
            }
            Debug.log("Couldn't replace goal '%s' for entity %s\n has goals: \n %s", goalName,
                nmsMob.getClass().getName(), String.join("\n ", gs));
            return false;
        }
        selector.addGoal(priority, newGoal.apply(nmsMob));
        return true;
    }

    protected void replaceEntity(
        org.bukkit.entity.Entity bukkitEntity, Function<Entity, Entity> constructor)
    {
        Entity nmsSource = (Entity) BukkitReflector.getEntityHandle(bukkitEntity);
        Entity replacement = constructor.apply(nmsSource);

        // get passengers
        List<Entity> passengers = nmsSource.getPassengers();
        nmsSource.ejectPassengers();

        // copies all the basic data
        replacement.restoreFrom(nmsSource);

        // remove old entity
        // need to use the dimension change one, because others error since the entities are now linked
        this.Entity_removeAfterChangingDimensions.invoke(nmsSource);

        nmsSource.level().addFreshEntity(replacement);

        // restore passengers
        for (Entity passenger : passengers) {
            passenger.startRiding(replacement, true, false);
        }
    }

    @Override
    public boolean clipWorld(
        Object level, Vector from, Vector to, BlockContext block, FluidContext fluid, Object sourceEntity)
    {
        ClipContext.Fluid fluidContext = switch (fluid) {
            case ANY -> ClipContext.Fluid.ANY;
            case WATER -> ClipContext.Fluid.WATER;
            case NONE -> ClipContext.Fluid.NONE;
        };

        ClipContext.Block blockContext = switch (block) {
            case COLLIDER -> ClipContext.Block.COLLIDER;
            case OUTLINE -> ClipContext.Block.OUTLINE;
            case VISUAL -> ClipContext.Block.VISUAL;
        };

        return ((Level) level).clip(new ClipContext(
            new Vec3(from.getX(), from.getY(), from.getZ()),
            new Vec3(to.getX(), to.getY(), to.getZ()),
            blockContext, fluidContext, (Entity) sourceEntity)).getType() != HitResult.Type.MISS;
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
            yValues = new double[]{targetPos.getY() + ((Entity) target).getEyeHeight()};
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
    public void setSwimPose(org.bukkit.entity.Player player) {
        ((ServerPlayer) BukkitReflector.getEntityHandle(player)).setPose(Pose.SWIMMING);
    }

    @Override
    public void addCrawlPoseWrapper(org.bukkit.entity.Player player) {
        ServerPlayer nsmPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);
        Pose pose = nsmPlayer.getPose();

        @SuppressWarnings("unchecked")
        EntityDataAccessor<Pose> dataPose = (EntityDataAccessor<Pose>) this.Entity_DATA_POSE.get();

        placeDataItem(new CrawlPoseDataItem(dataPose, Pose.STANDING, player), dataPose.id(), nsmPlayer.getEntityData());

        // restore old pose
        nsmPlayer.setPose(pose);
    }

    protected void placeDataItem(Object dataItem, int id, Object entityData) {
        Object[] idMap = (Object[]) this.SynchedEntityData_itemsById.get(entityData);
        if (id < idMap.length) {
            idMap[id] = dataItem;
        } else {
            Debug.log("Data pose index is higher than data array size???");
        }
    }

    @Override
    public @Nullable Object getHandItemInternal(org.bukkit.entity.Player player, VRBodyPart hand) {
        ServerPlayer nsmPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);
        if (hand.isHand()) {
            return nsmPlayer.getItemBySlot(
                hand == VRBodyPart.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setHandItemInternal(org.bukkit.entity.Player player, VRBodyPart hand, @Nullable Object itemStack) {
        if (hand.isHand()) {
            if (itemStack == null) {
                itemStack = ItemStack.EMPTY;
            }
            Inventory inventory = ((ServerPlayer) BukkitReflector.getEntityHandle(player)).getInventory();
            inventory.setItem(hand == VRBodyPart.MAIN_HAND ? inventory.getSelectedSlot() : Inventory.SLOT_OFFHAND,
                (ItemStack) itemStack);
        }
    }

    @Override
    public void applyEquipmentChange(org.bukkit.entity.Player player, Object oldItemStack, Object newItemStack) {
        ItemStack oldItem = (ItemStack) oldItemStack;
        ItemStack newItem = (ItemStack) newItemStack;
        if (!ItemStack.matches(oldItem, newItem)) {
            ServerPlayer nsmPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);
            AttributeMap attributes = nsmPlayer.getAttributes();
            if (!oldItem.isEmpty()) {
                oldItem.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                    AttributeInstance attribute = attributes.getInstance(holder);
                    if (attribute != null && modifier != null) {
                        attribute.removeModifier(modifier);
                    }
                });
            }
            if (!newItem.isEmpty()) {
                newItem.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                    AttributeInstance attribute = attributes.getInstance(holder);
                    if (attribute != null && modifier != null) {
                        attribute.removeModifier(modifier);
                        attribute.addTransientModifier(modifier);
                    }
                });
            }
        }
    }

    @Override
    public Object getItemStackCopy(Object itemStack) {
        if (itemStack == null) {
            return null;
        } else {
            return ((ItemStack) itemStack).copy();
        }
    }

    @Override
    public boolean itemStackMatch(Object nmsStack1, Object nmsStack2) {
        return ItemStack.matches((ItemStack) nmsStack1, (ItemStack) nmsStack2);
    }

    @Override
    public boolean inReducedAttackRange(org.bukkit.entity.Player player, org.bukkit.entity.Entity entity) {
        Entity nmsEntity = (Entity) BukkitReflector.getEntityHandle(entity);
        if (nmsEntity instanceof Mob mob) {
            AABB attackAABB = this.getAttackAABB(mob);
            attackAABB = attackAABB.inflate(
                ViveMain.CONFIG.mobAttackRangeAdjustment.get(),
                0,
                ViveMain.CONFIG.mobAttackRangeAdjustment.get());
            return attackAABB.intersects(
                (AABB) this.LivingEntity_getHitbox.invoke(BukkitReflector.getEntityHandle(player))) ||
                // if they stop let the attack through, or they will stand there forever
                entity.getVelocity().lengthSquared() <= 0.01;
        } else {
            // no attack range
            return true;
        }
    }

    protected AABB getAttackAABB(Mob nmsEntity) {
        AttackRange attackReach = nmsEntity.getActiveItem().get(DataComponents.ATTACK_RANGE);
        double range = attackReach == null ? (double) this.Mob_DEFAULT_ATTACK_REACH.get() :
            attackReach.effectiveMaxRange(nmsEntity);
        return (AABB) this.Mob_getAttackBoundingBox.invoke(nmsEntity, range);
    }

    @Override
    public Vector3fc getHitDirIfGate(Object player, Object packet) {
        if (packet instanceof ServerboundUseItemOnPacket useItemOnPacket) {
            ServerPlayer nmsPlayer = (ServerPlayer) player;
            BlockPos blockPos = useItemOnPacket.getHitResult().getBlockPos();
            Block block = nmsPlayer.level().getBlockState(blockPos).getBlock();
            if (block instanceof FenceGateBlock) {
                Vec3i dir = useItemOnPacket.getHitResult().getDirection().getUnitVec3i();
                if (dir.getY() == 0) {
                    return new Vector3f(-dir.getX(), -dir.getY(), -dir.getZ());
                }
            }
        }
        return null;
    }

    @Override
    public void playShieldBlockSound(org.bukkit.entity.Player player, org.bukkit.inventory.ItemStack itemStack) {
        BlocksAttacks blocksAttacks = ((ItemStack) BukkitReflector.asNMSCopy(itemStack)).get(
            DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null) {
            ServerPlayer nmsPlayer = (ServerPlayer) BukkitReflector.getEntityHandle(player);
            blocksAttacks.onBlocked(nmsPlayer.level(), nmsPlayer);
        }
    }
}
