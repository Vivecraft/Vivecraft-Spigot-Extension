package org.vivecraft.compat_impl.mc_1_16;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ShapedRecipe;
import org.vivecraft.compat_impl.mc_1_15.Api_1_15;

import java.util.List;

public class Api_1_16 extends Api_1_15 {

    @Override
    public void removeRecipes(List<ShapedRecipe> toRemove) {
        for (ShapedRecipe customRecipe : toRemove) {
            Bukkit.removeRecipe(customRecipe.getKey());
        }
    }

    @Override
    public boolean hasRecipe(ShapedRecipe recipe) {
        return Bukkit.getRecipe(recipe.getKey()) != null;
    }
}
