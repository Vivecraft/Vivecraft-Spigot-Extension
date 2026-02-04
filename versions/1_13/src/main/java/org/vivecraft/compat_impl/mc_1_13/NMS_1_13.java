package org.vivecraft.compat_impl.mc_1_13;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.vivecraft.accessors.*;
import org.vivecraft.compat.BukkitReflector;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.compat_impl.mc_1_12_2.NMS_1_12_2;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_13 extends NMS_1_12_2 {

    protected ReflectionField FluidClipContext_NONE;
    protected ReflectionField FluidClipContext_ANY;

    protected ReflectionMethod Component_getString;
    protected ReflectionMethod ItemStack_hasCustomHoverName;
    protected ReflectionMethod ItemStack_getHoverName;
    protected Class<?> TranslatableContents;
    protected ReflectionMethod TranslatableContents_getKey;

    @Override
    protected void init() {
        super.init();
        this.FluidClipContext_NONE = ReflectionField.getField(ClipContext$FluidMapping.FIELD_NONE,
            RayTraceFluidModeMapping.FIELD_NEVER);
        this.FluidClipContext_ANY = ReflectionField.getField(ClipContext$FluidMapping.FIELD_ANY,
            RayTraceFluidModeMapping.FIELD_ALWAYS);

        this.ItemStack_hasCustomHoverName = ReflectionMethod.getMethod(false,
            ItemStackMapping.METHOD_HAS_CUSTOM_HOVER_NAME);

        this.Component_getString = ReflectionMethod.getMethod(ComponentMapping.METHOD_GET_STRING);
        this.ItemStack_getHoverName = ReflectionMethod.getMethod(false, ItemStackMapping.METHOD_GET_HOVER_NAME);
        this.TranslatableContents = ClassGetter.getClass(true, TranslatableContentsMapping.MAPPING);
        this.TranslatableContents_getKey = ReflectionMethod.getMethod(TranslatableContentsMapping.METHOD_GET_KEY);
    }

    @Override
    public boolean clipWorld(
        Object level, Vector from, Vector to, BlockContext block, FluidContext fluid, Object sourceEntity)
    {
        return this.Level_Clip.invoke(level,
            this.Vec3.newInstance(from.getX(), from.getY(), from.getZ()),
            this.Vec3.newInstance(to.getX(), to.getY(), to.getZ()),
            fluid == FluidContext.NONE ? this.FluidClipContext_NONE.get() : this.FluidClipContext_ANY.get(),
            block != BlockContext.COLLIDER, false) != null;
    }


    @Override
    public boolean hasItemStackName(ItemStack itemStack, String translationKey, String fallback) {
        Object nmsStack = BukkitReflector.asNMSCopy(itemStack);
        if (hasCustomHoverName(nmsStack)) {
            Object component = this.ItemStack_getHoverName.invoke(nmsStack);
            if (fallback.equals(this.Component_getString.invoke(component))) {
                return true;
            }
            Object content = this.getTranslationComponent(component);
            return this.TranslatableContents.isInstance(content) &&
                translationKey.equals(this.TranslatableContents_getKey.invoke(content));
        }
        return false;
    }

    protected Object getTranslationComponent(Object component) {
        return component;
    }

    protected boolean hasCustomHoverName(Object nmsStack) {
        return (boolean) this.ItemStack_hasCustomHoverName.invoke(nmsStack);
    }
}
