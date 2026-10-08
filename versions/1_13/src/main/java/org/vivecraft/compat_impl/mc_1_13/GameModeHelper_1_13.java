package org.vivecraft.compat_impl.mc_1_13;

import org.vivecraft.accessors.BlockBehaviour$BlockStateBaseMapping;
import org.vivecraft.accessors.BlockStateMapping;
import org.vivecraft.compat_impl.mc_1_9.GameModeHelper_1_9;
import org.vivecraft.util.reflection.ReflectionMethod;

public class GameModeHelper_1_13 extends GameModeHelper_1_9 {

    protected ReflectionMethod BlockState_isAir;

    @Override
    protected void initAir() {
        this.BlockState_isAir = ReflectionMethod.getMethod(BlockBehaviour$BlockStateBaseMapping.METHOD_IS_AIR,
            BlockStateMapping.METHOD_IS_AIR);
    }

    @Override
    protected boolean isAir(Object blockState) {
        return (boolean) this.BlockState_isAir.invoke(blockState);
    }
}
