package com.kitsunetech.fishing;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerFishEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Item 3: EventManager.getXpMultiplier must be wired into the XP path so an active
 * seasonal event (Double-XP day) boosts fishing XP, while no event leaves it normal.
 */
class EventWiringTest extends FishingTestBase {

    @Test
    @DisplayName("Double-XP day boosts a catch's XP; no event leaves it normal")
    void doubleXpDayBoostsXp() {
        // Keep the streak below the frenzy threshold so the event is the only multiplier.
        setConfig("streak.frenzy-threshold", 100);

        String today = LocalDate.now().getDayOfWeek().name();

        // Event active: today is a Double-XP day -> XP doubles.
        setConfig("events.double-xp-days", List.of(today));
        PlayerFishEvent active = fireCatch(Material.COD, 10);
        assertEquals(20, active.getExpToDrop(), "Double-XP day should double the fishing XP");

        // No event: clear the Double-XP days -> XP unchanged.
        setConfig("events.double-xp-days", Collections.emptyList());
        PlayerFishEvent normal = fireCatch(Material.COD, 10);
        assertEquals(10, normal.getExpToDrop(), "with no event the XP is unchanged");
    }

    @Test
    @DisplayName("EventManager reports Double-XP only on configured days")
    void eventManagerReflectsConfig() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        setConfig("events.double-xp-days", List.of(today.name()));
        assertEquals(2.0, plugin.getEventManager().getXpMultiplier(player), 0.0001);

        setConfig("events.double-xp-days", Collections.emptyList());
        assertEquals(1.0, plugin.getEventManager().getXpMultiplier(player), 0.0001);
    }

    @Test
    @DisplayName("rain uses the configured loot multiplier once")
    void rainUsesConfiguredLootMultiplier() {
        setConfig("events.rain-bonus", 1.35);
        world.setStorm(true);

        assertEquals(1.35, plugin.getEventManager().getLootMultiplier(player), 0.0001);
    }
}
