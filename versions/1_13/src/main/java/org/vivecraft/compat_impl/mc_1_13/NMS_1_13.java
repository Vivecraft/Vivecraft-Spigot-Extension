package org.vivecraft.compat_impl.mc_1_13;

import org.bukkit.util.Vector;
import org.vivecraft.accessors.ClipContext$FluidMapping;
import org.vivecraft.accessors.RayTraceFluidModeMapping;
import org.vivecraft.compat.types.BlockContext;
import org.vivecraft.compat.types.FluidContext;
import org.vivecraft.compat_impl.mc_1_12_2.NMS_1_12_2;
import org.vivecraft.util.reflection.ReflectionField;

public class NMS_1_13 extends NMS_1_12_2 {

    protected ReflectionField FluidClipContext_NONE;
    protected ReflectionField FluidClipContext_ANY;

    @Override
    protected void init() {
        super.init();
        this.FluidClipContext_NONE = ReflectionField.getField(ClipContext$FluidMapping.FIELD_NONE,
            RayTraceFluidModeMapping.FIELD_NEVER);
        this.FluidClipContext_ANY = ReflectionField.getField(ClipContext$FluidMapping.FIELD_ANY,
            RayTraceFluidModeMapping.FIELD_ALWAYS);
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
}
