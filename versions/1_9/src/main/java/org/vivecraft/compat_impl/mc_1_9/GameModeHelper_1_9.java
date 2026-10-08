package org.vivecraft.compat_impl.mc_1_9;

import org.vivecraft.accessors.IBlockPropertiesMapping;
import org.vivecraft.accessors.MaterialMapping;
import org.vivecraft.compat_impl.mc_1_8.GameModeHelper_1_8;
import org.vivecraft.util.reflection.ReflectionField;
import org.vivecraft.util.reflection.ReflectionMethod;

public class GameModeHelper_1_9 extends GameModeHelper_1_8 {
    private ReflectionMethod BlockState_getMaterial;
    private ReflectionField Material_AIR;

    @Override
    protected void initAir() {
        this.Material_AIR = ReflectionField.getField(MaterialMapping.FIELD_AIR);
        this.BlockState_getMaterial = ReflectionMethod.getMethod(IBlockPropertiesMapping.METHOD_GET_MATERIAL);
    }

    @Override
    protected boolean isAir(Object blockState) {
        return this.BlockState_getMaterial.invoke(blockState) == this.Material_AIR.get();
    }
}
