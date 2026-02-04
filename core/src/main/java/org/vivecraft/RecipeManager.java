package org.vivecraft;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.vivecraft.util.Utils;

import java.util.*;

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
            if (!hasRecipe(recipe)) {
                Bukkit.addRecipe(recipe);
            }
        }
    }

    public void removeRecipes(List<ShapedRecipe> toRemove) {
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            for (ShapedRecipe customRecipe : toRemove) {
                if (recipeEquals(customRecipe, recipe)) {
                    recipes.remove();
                }
            }
        }
    }

    private boolean hasRecipe(ShapedRecipe recipe) {
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            if (recipeEquals(recipe, recipes.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean recipeEquals(ShapedRecipe customRecipe, Recipe other) {
        if (!(other instanceof ShapedRecipe) || other.getResult().getType() != customRecipe.getResult().getType()) {
            return false;
        }

        ShapedRecipe otherRecipe = (ShapedRecipe) other;

        if (otherRecipe.getShape().length != customRecipe.getShape().length) return false;

        Map<Character, ItemStack> customItems = customRecipe.getIngredientMap();
        Map<Character, ItemStack> otherItems = otherRecipe.getIngredientMap();

        if (Utils.containsAll(customItems.values(), otherItems.values())) {
            String customShape = String.join("", customRecipe.getShape());
            String otherShape = String.join("", otherRecipe.getShape());

            for (int i = 0; i < customShape.length(); i++) {
                ItemStack customItem = customItems.get(customShape.charAt(i));
                ItemStack otherItem = otherItems.get(otherShape.charAt(i));
                if (!Objects.equals(customItem, otherItem)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
