package com.kitsunetech.fishing;

import com.kitsunetech.fishing.items.CustomItems;
import com.kitsunetech.fishing.systems.FishJournal;
import com.kitsunetech.fishing.systems.LeaderboardManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundaryAndEdgeCaseTest extends FishingTestBase {

    @Test
    void cancelledFishingDoesNotAwardProgression() {
        Item caught = world.dropItem(player.getLocation(), new ItemStack(Material.COD));
        PlayerFishEvent event = new PlayerFishEvent(player, caught, null, PlayerFishEvent.State.CAUGHT_FISH);
        event.setExpToDrop(10);
        event.setCancelled(true);

        server.getPluginManager().callEvent(event);

        assertEquals(0, plugin.getFisherDataManager().getFisherData(player.getUniqueId()).getTotalFishCaught());
        assertEquals(0, plugin.getStreakManager().getStreak(player.getUniqueId()));
        assertTrue(plugin.getFishJournal().getJournal(player.getUniqueId()).catches.isEmpty());
    }

    @Test
    void cancelledItemInteractionDoesNotConsumeCustomItem() {
        ItemStack ledger = CustomItems.createNeptunesLedger();
        player.getInventory().setItemInMainHand(ledger);

        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_AIR, ledger, null, null, EquipmentSlot.HAND);
        event.setCancelled(true);
        server.getPluginManager().callEvent(event);

        assertEquals(1, ledger.getAmount());
        assertEquals(0.0, plugin.getFisherDataManager().getFisherData(player.getUniqueId())
                .getPermanentSpeedBonus(), 0.0001);
    }

    @Test
    void netherForestUsesTheNetherFishingGate() {
        world.setEnvironment(World.Environment.NETHER);
        world.setBiome(player.getLocation(), Biome.CRIMSON_FOREST);

        setConfig("events.blood-moon.enabled", false);
        setConfig("drops.ultra-rare-base", 0.0);
        setConfig("drops.rare-base", 0.0);
        setConfig("drops.uncommon-base", 2.0);
        setConfig("drops.common-base", 0.0);
        setConfig("features.nether-fishing-level", 20);

        assertNull(plugin.getLootManager().generateLoot(player,
                plugin.getFisherDataManager().getFisherData(player.getUniqueId())));

        setConfig("features.nether-fishing-level", 1);
        ItemStack loot = plugin.getLootManager().generateLoot(player,
                plugin.getFisherDataManager().getFisherData(player.getUniqueId()));
        assertTrue(loot.getType() == Material.GHAST_TEAR || loot.getType() == Material.MAGMA_CREAM);
    }

    @Test
    void journalClassifiesNetherForestAndProtectsItsGroupSet() {
        assertEquals("nether", FishJournal.getBiomeGroup(Biome.CRIMSON_FOREST));
        assertEquals("other", FishJournal.getBiomeGroup(null));
        assertThrows(UnsupportedOperationException.class,
                () -> FishJournal.getAllBiomeGroups().clear());
    }

    @Test
    void leaderboardHandlesNegativeCountsAndExtremeIntegerValues() {
        setConfig("leaderboard.top-count", -1);
        assertEquals(0, plugin.getConfigManager().getTopCount());
        assertTrue(plugin.getLeaderboardManager().getTop(-1).isEmpty());

        UUID highId = UUID.randomUUID();
        UUID lowId = UUID.randomUUID();
        LeaderboardManager manager = plugin.getLeaderboardManager();
        manager.updatePlayer(highId, "High", Integer.MAX_VALUE, 0);
        manager.updatePlayer(lowId, "Low", Integer.MIN_VALUE, 0);

        assertSame(highId, manager.getTop(2).get(0).playerId);
    }

    @Test
    void dayNamesAreParsedIndependentlyOfDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            List<String> days = new ArrayList<>();
            days.add(null);
            days.add(" friday ");
            setConfig("events.double-xp-days", days);
            assertTrue(plugin.getConfigManager().getDoubleXpDays().contains(DayOfWeek.FRIDAY));

            List<String> drops = new ArrayList<>();
            drops.add(null);
            drops.add(" ghast_tear ");
            setConfig("events.blood-moon.drops", drops);
            assertTrue(plugin.getConfigManager().getBloodMoonDrops().contains(Material.GHAST_TEAR));
        } finally {
            Locale.setDefault(previous);
        }
    }
}
