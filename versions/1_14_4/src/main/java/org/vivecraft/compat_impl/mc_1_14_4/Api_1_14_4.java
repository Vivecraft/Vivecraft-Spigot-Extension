package org.vivecraft.compat_impl.mc_1_14_4;

import org.bukkit.entity.Arrow;
import org.vivecraft.compat_impl.mc_1_14.Api_1_14;

public class Api_1_14_4 extends Api_1_14 {

    @Override
    public boolean isArrowPiercing(Arrow arrow) {
        return arrow.getPierceLevel() > 0;
    }
}
