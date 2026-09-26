package org.vivecraft;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private final List<ShapedRecipe> climbeyRecipes = new ArrayList<>();

    public RecipeManager() {
        this.createClawsRecipe();
        this.createBootsRecipe();
    }

    private void createClawsRecipe() {
        ItemStack claws = new ItemStack(Material.SHEARS);

        if (!ViveMain.API.setItemStackUnbreakable(claws, true)) {
            ViveMain.LOGGER.info("Error creating claws recipe, not added");
            return;
        }
        claws = ViveMain.NMS.setItemStackName(claws, "vivecraft.item.climbclaws", "Climb Claws");

        ShapedRecipe clawsRecipe = ViveMain.API.createRecipe(claws, "climb_claws");
        clawsRecipe.shape("E E", "S S");
        clawsRecipe.setIngredient('E', Material.SPIDER_EYE);
        clawsRecipe.setIngredient('S', Material.SHEARS);
        this.climbeyRecipes.add(clawsRecipe);
    }

    public static boolean isClimbingClaw(ItemStack stack) {
        if (stack == null) {
            return false;
        } else if (stack.getType() != Material.SHEARS) {
            return false;
        } else if (!ViveMain.API.isItemStackUnbreakable(stack)) {
            return false;
        } else {
            return ViveMain.NMS.hasItemStackName(stack, "vivecraft.item.climbclaws", "Climb Claws");
        }
    }

    private void createBootsRecipe() {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        if (!ViveMain.API.setItemStackUnbreakable(boots, true)) {
            ViveMain.LOGGER.info("Error creating boots recipe, not added");
            return;
        }
        boots = ViveMain.NMS.setItemStackName(boots, "vivecraft.item.jumpboots", "Jump Boots");

        ItemMeta bootsMeta = boots.getItemMeta();
        ((LeatherArmorMeta) bootsMeta).setColor(Color.fromRGB(0x8CE56F));
        boots.setItemMeta(bootsMeta);

        ShapedRecipe bootsRecipe = ViveMain.API.createRecipe(boots, "jump_boots");
        bootsRecipe.shape("B", "S");
        bootsRecipe.setIngredient('B', Material.LEATHER_BOOTS);
        bootsRecipe.setIngredient('S', Material.SLIME_BLOCK);
        this.climbeyRecipes.add(bootsRecipe);
    }

    public void updateRecipes() {
        if (ViveMain.CONFIG.viveCrafting.get() && ViveMain.CONFIG.climbeyEnabled.get()) {
            addRecipes(this.climbeyRecipes);
        } else {
            removeRecipes(this.climbeyRecipes);
        }
    }

    public void addRecipes(List<ShapedRecipe> toAdd) {
        for (ShapedRecipe recipe : toAdd) {
            if (!ViveMain.API.hasRecipe(recipe)) {
                Bukkit.addRecipe(recipe);
            }
        }
    }

    public void removeRecipes(List<ShapedRecipe> toRemove) {
        ViveMain.API.removeRecipes(toRemove);
    }
}
