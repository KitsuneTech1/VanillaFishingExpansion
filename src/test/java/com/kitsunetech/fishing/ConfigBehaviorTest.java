package com.kitsunetech.fishing;

import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigBehaviorTest extends FishingTestBase {

    @Test
    @DisplayName("leveling settings control progression and the maximum level")
    void levelingSettingsAreApplied() {
        setConfig("leveling.base-fish-required", 2);
        setConfig("leveling.scaling-multiplier", 2.0);
        setConfig("leveling.max-level", 2);
        setConfig("events.double-xp-days", java.util.Collections.emptyList());

        fireCatch(Material.COD, 1);
        assertEquals(1, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getLevel());
        assertEquals(1, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getFishForCurrentLevel());

        fireCatch(Material.COD, 1);
        assertEquals(2, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getLevel());
        assertEquals(0, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getFishForCurrentLevel());

        fireCatch(Material.COD, 1);
        assertEquals(2, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getLevel());
        assertEquals(1, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getFishForCurrentLevel());
        assertEquals(2, plugin.getConfigManager().getMaxLevel());
    }

    @Test
    @DisplayName("configured drop rates control global loot and the Angler's Rod drop")
    void dropSettingsAreApplied() {
        setConfig("drops.ultra-rare-base", 0.0);
        setConfig("drops.rare-base", 0.0);
        setConfig("drops.uncommon-base", 0.0);
        setConfig("drops.common-base", 2.0);
        setConfig("features.anglers-rod", false);

        ItemStack globalLoot = plugin.getLootManager().generateLoot(player,
                plugin.getFisherDataManager().getFisherData(player.getUniqueId()));
        assertNotNull(globalLoot, "a 100% effective global chance should produce loot");

        setConfig("drops.common-base", 0.0);
        setConfig("features.anglers-rod", true);
        setConfig("items.anglers-rod.drop-chance", 1.0);

        ItemStack rod = plugin.getLootManager().generateLoot(player,
                plugin.getFisherDataManager().getFisherData(player.getUniqueId()));
        assertEquals(CustomItems.ANGLERS_ROD, CustomItems.getCustomItemId(rod));
    }
}
