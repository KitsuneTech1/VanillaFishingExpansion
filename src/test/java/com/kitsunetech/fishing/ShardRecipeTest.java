package com.kitsunetech.fishing;

import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Item 4: the shard recipes must require the plugin's PDC-tagged custom shard items,
 * not plain vanilla mats, so a Trident/Totem can only be crafted from real shards.
 */
class ShardRecipeTest extends FishingTestBase {

    @Test
    @DisplayName("Trident recipe rejects plain prismarine shards, accepts real Trident Shards")
    void tridentRecipeRequiresCustomShard() {
        ShapedRecipe recipe = plugin.buildTridentRecipe();
        assertEquals(Material.TRIDENT, recipe.getResult().getType(), "recipe still yields a Trident");

        RecipeChoice choice = recipe.getChoiceMap().get('S');
        assertNotNull(choice, "ingredient 'S' must be defined");

        assertFalse(choice.test(new ItemStack(Material.PRISMARINE_SHARD)),
                "plain vanilla prismarine shards must NOT satisfy the recipe");
        assertTrue(choice.test(CustomItems.createTridentShard()),
                "the real custom Trident Shard must satisfy the recipe");
    }

    @Test
    @DisplayName("Totem recipe rejects plain gold nuggets, accepts real Totem Shards")
    void totemRecipeRequiresCustomShard() {
        ShapedRecipe recipe = plugin.buildTotemRecipe();
        assertEquals(Material.TOTEM_OF_UNDYING, recipe.getResult().getType(), "recipe still yields a Totem");

        RecipeChoice choice = recipe.getChoiceMap().get('S');
        assertNotNull(choice, "ingredient 'S' must be defined");

        assertFalse(choice.test(new ItemStack(Material.GOLD_NUGGET)),
                "plain vanilla gold nuggets must NOT satisfy the recipe");
        assertTrue(choice.test(CustomItems.createTotemShard()),
                "the real custom Totem Shard must satisfy the recipe");
    }
}
