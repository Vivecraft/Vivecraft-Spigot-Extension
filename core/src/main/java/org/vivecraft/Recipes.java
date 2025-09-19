package org.vivecraft;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Recipes {

    public static void addClawsRecipe() {
        ItemStack claws = new ItemStack(Material.SHEARS);

        if (!ViveMain.API.setItemStackUnbreakable(claws, true)) {
            ViveMain.LOGGER.info("Error creating boots recipe, not added");
            return;
        }
        claws = ViveMain.NMS.setItemStackName(claws, "vivecraft.item.climbclaws", "Climb Claws");

        ShapedRecipe clawsRecipe = ViveMain.API.createRecipe(claws, "climb_claws");
        clawsRecipe.shape("E E", "S S");
        clawsRecipe.setIngredient('E', Material.SPIDER_EYE);
        clawsRecipe.setIngredient('S', Material.SHEARS);
        Bukkit.addRecipe(clawsRecipe);
    }

    public static void addBootsRecipe() {
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
        Bukkit.addRecipe(bootsRecipe);
    }
}
