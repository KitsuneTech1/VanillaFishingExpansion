package com.kitsunetech.fishing;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Item 2: FishJournal.recordCatch must be wired into the CAUGHT_FISH handler so each
 * catch is recorded, and the record must survive an unload/reload cycle.
 */
class JournalWiringTest extends FishingTestBase {

    @Test
    @DisplayName("A catch is recorded in the journal and persists across a reload")
    void catchIsRecordedAndPersists() {
        UUID id = player.getUniqueId();

        assertFalse(plugin.getFishJournal().getJournal(id).catches.contains(Material.COD),
                "journal starts empty");

        PlayerFishEvent event = fireCatch(Material.COD, 1);
        // Read the final caught material (custom loot may replace the vanilla catch).
        Material caught = ((Item) event.getCaught()).getItemStack().getType();

        assertTrue(plugin.getFishJournal().getJournal(id).catches.contains(caught),
                "the caught item is recorded in the journal");

        // Unload writes the journal to disk and drops it from memory; re-fetching reloads it.
        plugin.getFishJournal().unloadPlayer(id);
        assertTrue(plugin.getFishJournal().getJournal(id).catches.contains(caught),
                "the recorded catch persists across a reload");
    }

    @Test
    @DisplayName("total progress counts the same material separately in each biome")
    void totalProgressCountsBiomeEntries() {
        UUID id = player.getUniqueId();

        plugin.getFishJournal().recordCatch(id, Material.COD, "ocean");
        plugin.getFishJournal().recordCatch(id, Material.COD, "forest");

        assertEquals(2.0 / 34.0, plugin.getFishJournal().getTotalProgress(id), 0.0001);
    }
}
