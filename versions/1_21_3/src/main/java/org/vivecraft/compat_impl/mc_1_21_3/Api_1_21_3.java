package org.vivecraft.compat_impl.mc_1_21_3;

import org.bukkit.attribute.Attribute;
import org.vivecraft.compat_impl.mc_1_21.Api_1_21;

public class Api_1_21_3 extends Api_1_21 {

    @Override
    protected Attribute getArmorAttribute() {
        return Attribute.ARMOR;
    }
}
