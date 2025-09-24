package org.vivecraft.compat_impl.mc_1_11;

import org.vivecraft.compat_impl.mc_1_9.EndermanHelper_1_9;

import java.util.List;

public class EndermanHelper_1_11 extends EndermanHelper_1_9 {

    @Override
    public boolean hasProtection(Object nmsPlayer) {
        Object headitem = ((List) this.Inventory_armor.get(this.Player_inventory.get(nmsPlayer))).get(3);
        return this.ItemStack_getItem.invoke(headitem) == this.Item_byBlock.invokes(this.Blocks_CARVED_PUMPKIN.get());
    }
}
