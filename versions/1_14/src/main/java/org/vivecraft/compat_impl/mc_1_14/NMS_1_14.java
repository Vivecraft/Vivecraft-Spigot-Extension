package org.vivecraft.compat_impl.mc_1_14;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.compat_impl.mc_1_13_2.NMS_1_13_2;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Collection;
import java.util.Map;

public class NMS_1_14 extends NMS_1_13_2 {
    protected ReflectionField AttributeModifierOperation_ADD_VALUE;
    protected ReflectionField AttributeModifierOperation_ADD_MULTIPLIED_TOTAL;

    protected ReflectionConstructor ClipContext;
    protected ReflectionField FluidClipContext_WATER;
    protected ReflectionField BlockClipContext_COLLIDER;
    protected ReflectionField BlockClipContext_OUTLINE;
    protected ReflectionField BlockClipContext_VISUAL;
    protected ReflectionMethod BlockHitResult_getType;
    protected ReflectionField HitResultType_MISS;

    protected ReflectionField Pose_STANDING;
    protected ReflectionField Pose_SWIMMING;
    protected ReflectionMethod Entity_getPose;
    protected ReflectionMethod Entity_setPose;
    protected ReflectionField Entity_DATA_POSE;
    protected ReflectionMethod Entity_getEntityData;
    protected ReflectionField SynchedEntityData_itemsById;
    protected ReflectionMethod DataItem_getAccessor;
    protected ReflectionMethod EntityDataAccessor_id;

    protected ReflectionConstructor CrawlPoseDataItem_Constructor;

    @Override
    protected void init() {
        super.init();
        this.FluidClipContext_WATER = ReflectionField.getField(false, ClipContext$FluidMapping.FIELD_WATER);
        this.BlockClipContext_COLLIDER = ReflectionField.getField(ClipContext$BlockMapping.FIELD_COLLIDER);
        this.BlockClipContext_OUTLINE = ReflectionField.getField(ClipContext$BlockMapping.FIELD_OUTLINE);
        this.BlockClipContext_VISUAL = ReflectionField.getField(false, ClipContext$BlockMapping.FIELD_VISUAL);
        this.ClipContext = ReflectionConstructor.getConstructor(ClipContextMapping.CONSTRUCTOR_0);
        this.BlockHitResult_getType = ReflectionMethod.getMethod(BlockHitResultMapping.METHOD_GET_TYPE);
        this.HitResultType_MISS = ReflectionField.getField(HitResult$TypeMapping.FIELD_MISS);

        this.Entity_getPose = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_POSE);
        this.Entity_setPose = ReflectionMethod.getMethod(EntityMapping.METHOD_SET_POSE);
        this.Pose_STANDING = ReflectionField.getField(PoseMapping.FIELD_STANDING);
        this.Pose_SWIMMING = ReflectionField.getField(PoseMapping.FIELD_SWIMMING);

        this.Entity_DATA_POSE = ReflectionField.getField(EntityMapping.FIELD_DATA_POSE);
        this.Entity_getEntityData = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_ENTITY_DATA);
        this.SynchedEntityData_itemsById = ReflectionField.getField(SynchedEntityDataMapping.FIELD_ITEMS_BY_ID);
        this.DataItem_getAccessor = ReflectionMethod.getMethod(SynchedEntityData$DataItemMapping.METHOD_GET_ACCESSOR);
        this.EntityDataAccessor_id = ReflectionMethod.getMethod(EntityDataAccessorMapping.METHOD_ID,
            EntityDataAccessorMapping.METHOD_GET_ID);

        this.CrawlPoseDataItem_Constructor = ReflectionConstructor.getCompat("CrawlPoseDataItem",
            ClassGetter.getClass(true, EntityDataAccessorMapping.MAPPING),
            ClassGetter.getClass(true, PoseMapping.MAPPING), Player.class);
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.AttributeModifierOperation_ADD_VALUE = ReflectionField.getField(
            AttributeModifier$OperationMapping.FIELD_ADD_VALUE);
        this.AttributeModifierOperation_ADD_MULTIPLIED_TOTAL = ReflectionField.getField(
            AttributeModifier$OperationMapping.FIELD_ADD_MULTIPLIED_TOTAL);
    }

    @Override
    protected double applyAttributeModifiers(double original, Collection<Object> modifiers) {
        for (Object modifier : modifiers) {
            double amount = (double) this.AttributeModifier_getAmount.invoke(modifier);
            Object operation = this.AttributeModifier_getOperation.invoke(modifier);
            if (operation == this.AttributeModifierOperation_ADD_VALUE.get()) {
                original += amount;
            } else if (operation == this.AttributeModifierOperation_ADD_MULTIPLIED_TOTAL.get()) {
                original += amount * original;
            }
        }
        return original;
    }

    @Override
    public boolean clipWorld(
        Object level, Vector from, Vector to, BlockContext block, FluidContext fluid, Object sourceEntity)
    {
        Object fluidContext = null;
        switch (fluid) {
            case ANY:
                fluidContext = this.FluidClipContext_ANY.get();
                break;
            case WATER:
                if (this.FluidClipContext_WATER != null) {
                    fluidContext = this.FluidClipContext_WATER.get();
                } else {
                    fluidContext = this.FluidClipContext_NONE.get();
                }
                break;
            case NONE:
                fluidContext = this.FluidClipContext_NONE.get();
                break;
        }
        Object blockContext = null;
        switch (block) {
            case COLLIDER:
                blockContext = this.BlockClipContext_COLLIDER.get();
                break;
            case OUTLINE:
                blockContext = this.BlockClipContext_OUTLINE.get();
                break;
            case VISUAL:
                if (this.BlockClipContext_VISUAL != null) {
                    blockContext = this.BlockClipContext_VISUAL.get();
                } else {
                    blockContext = this.BlockClipContext_OUTLINE.get();
                }
                break;
        }
        return this.BlockHitResult_getType.invoke(this.Level_Clip.invoke(level,
            this.ClipContext.newInstance(
                this.Vec3.newInstance(from.getX(), from.getY(), from.getZ()),
                this.Vec3.newInstance(to.getX(), to.getY(), to.getZ()),
                blockContext, fluidContext, sourceEntity))) != this.HitResultType_MISS.get();
    }

    @Override
    public void modifyEntity(Entity entity) {
        if (entity instanceof Enderman) {
            if (!replaceGoal(entity, false, goal -> ViveMain.MC_MODS.endermanHelper().isFreezeGoal(goal),
                enderman -> ViveMain.MC_MODS.endermanHelper().getEndermanFreezeWhenLookAt(enderman)))
            {
                throw new RuntimeException("Could not find freezewhenlookat goal for enderman");
            }
        }
        super.modifyEntity(entity);
    }

    @Override
    public void setSwimPose(Player player) {
        this.Entity_setPose.invoke(BukkitReflector.getEntityHandle(player), this.Pose_SWIMMING.get());
    }

    @Override
    public void addCrawlPoseWrapper(Player player) {
        Object nsmPlayer = BukkitReflector.getEntityHandle(player);
        Object pose = this.Entity_getPose.invoke(nsmPlayer);

        placeDataItem(this.CrawlPoseDataItem_Constructor.newInstance(this.Entity_DATA_POSE.get(),
                this.Pose_STANDING.get(), player),
            (int) this.EntityDataAccessor_id.invoke(this.Entity_DATA_POSE.get()),
            this.Entity_getEntityData.invoke(nsmPlayer));

        // restore old pose
        this.Entity_setPose.invoke(nsmPlayer, pose);
    }

    @SuppressWarnings("unchecked")
    protected void placeDataItem(Object dataItem, int id, Object entityData) {
        Map idMap = (Map) this.SynchedEntityData_itemsById.get(entityData);
        idMap.put(id, dataItem);
    }
}
