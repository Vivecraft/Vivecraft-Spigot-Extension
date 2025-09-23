package org.vivecraft.compat_impl.mc_1_14;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.vivecraft.ViveMain;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.compat_impl.mc_1_13_2.NMS_1_13_2;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

import java.util.Collection;

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

    @Override
    protected void init() {
        super.init();
        this.FluidClipContext_WATER = ReflectionField.getField(ClipContext$FluidMapping.FIELD_WATER);
        this.BlockClipContext_COLLIDER = ReflectionField.getField(ClipContext$BlockMapping.FIELD_COLLIDER);
        this.BlockClipContext_OUTLINE = ReflectionField.getField(ClipContext$BlockMapping.FIELD_OUTLINE);
        this.BlockClipContext_VISUAL = ReflectionField.getField(ClipContext$BlockMapping.FIELD_VISUAL);
        this.ClipContext = ReflectionConstructor.getConstructor(ClipContextMapping.CONSTRUCTOR_0);
        this.BlockHitResult_getType = ReflectionMethod.getMethod(BlockHitResultMapping.METHOD_GET_TYPE);
        this.HitResultType_MISS = ReflectionField.getField(HitResult$TypeMapping.FIELD_MISS);
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
                fluidContext = this.FluidClipContext_WATER.get();
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
                blockContext = this.BlockClipContext_VISUAL.get();
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
}
