package org.vivecraft.compat_impl.mc_1_12_2;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.vivecraft.ViveMain;
import org.vivecraft.compat_impl.mc_1_11.Api_1_11;

public class Api_1_12_2 extends Api_1_11 {

    @Override
    protected void init() {
    }

    @Override
    public float getEntityWidth(Entity entity) {
        return (float) entity.getWidth();
    }

    @Override
    public ShapedRecipe createRecipe(ItemStack itemStack, String id) {
        return new ShapedRecipe(new NamespacedKey(ViveMain.INSTANCE, id), itemStack);
    }

    @Override
    public boolean setItemStackUnbreakable(ItemStack itemStack, boolean hide) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        if (hide) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        itemStack.setItemMeta(meta);
        return true;
    }
}
