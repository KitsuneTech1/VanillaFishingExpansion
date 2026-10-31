package com.kitsunetech.fishing;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerFishEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Item 1: StreakManager.recordCatch must be wired into the CAUGHT_FISH handler so
 * catches build a streak, crossing the frenzy threshold applies the frenzy XP bonus,
 * and a failed attempt resets the streak.
 */
class StreakWiringTest extends FishingTestBase {

    @Test
    @DisplayName("Catches build a streak, frenzy applies the XP bonus, a miss resets it")
    void streakBuildsFrenzyAppliesAndMissResets() {
        UUID id = player.getUniqueId();

        // Low threshold + no seasonal XP event so the only multiplier is the frenzy one.
        setConfig("streak.frenzy-threshold", 3);
        setConfig("streak.frenzy-xp-multiplier", 2.0);
        setConfig("events.double-xp-days", Collections.emptyList());

        // Catch 1: streak 1, below threshold, XP unchanged.
        PlayerFishEvent e1 = fireCatch(Material.COD, 10);
        assertEquals(1, plugin.getStreakManager().getStreak(id), "first catch should start the streak at 1");
        assertFalse(plugin.getStreakManager().isInFrenzy(id), "not in frenzy yet");
        assertEquals(10, e1.getExpToDrop(), "no frenzy so base XP is untouched");

        // Catch 2: streak 2, still below threshold.
        PlayerFishEvent e2 = fireCatch(Material.COD, 10);
        assertEquals(2, plugin.getStreakManager().getStreak(id));
        assertFalse(plugin.getStreakManager().isInFrenzy(id));
        assertEquals(10, e2.getExpToDrop());

        // Catch 3: streak 3 hits the threshold -> frenzy active -> 2x XP.
        PlayerFishEvent e3 = fireCatch(Material.COD, 10);
        assertEquals(3, plugin.getStreakManager().getStreak(id));
        assertTrue(plugin.getStreakManager().isInFrenzy(id), "streak reached threshold so frenzy is active");
        assertEquals(20, e3.getExpToDrop(), "frenzy should double the fishing XP");

        // A miss (fish got away) resets the streak.
        fireFailedAttempt();
        assertEquals(0, plugin.getStreakManager().getStreak(id), "a failed attempt resets the streak");
        assertFalse(plugin.getStreakManager().isInFrenzy(id));
    }
}
