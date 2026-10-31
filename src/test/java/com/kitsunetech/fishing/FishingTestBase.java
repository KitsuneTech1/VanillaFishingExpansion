package com.kitsunetech.fishing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

/**
 * Shared MockBukkit harness: boots a mock server, loads the real plugin (which
 * runs onEnable and registers the real listeners), and gives each test a world
 * and a player plus helpers to fire fishing events through the live event bus.
 */
public abstract class FishingTestBase {

    protected ServerMock server;
    protected VanillaFishingExpansion plugin;
    protected WorldMock world;
    protected PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(VanillaFishingExpansion.class);
        world = server.addSimpleWorld("fishworld");
        player = server.addPlayer();
        player.teleport(new Location(world, 0, 64, 0));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Fire a real CAUGHT_FISH PlayerFishEvent for the given caught material with a
     * known base XP value, dispatched through the plugin manager so the registered
     * FishingListener actually handles it. Returns the (mutated) event.
     */
    protected PlayerFishEvent fireCatch(Material caughtType, int baseExp) {
        Item caught = world.dropItem(player.getLocation(), new ItemStack(caughtType));
        PlayerFishEvent event = new PlayerFishEvent(player, caught, null, PlayerFishEvent.State.CAUGHT_FISH);
        event.setExpToDrop(baseExp);
        server.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Fire a FAILED_ATTEMPT (the fish got away) event through the live event bus.
     */
    protected void fireFailedAttempt() {
        PlayerFishEvent event = new PlayerFishEvent(player, null, null, PlayerFishEvent.State.FAILED_ATTEMPT);
        server.getPluginManager().callEvent(event);
    }

    /**
     * Override config values, persist them, and reload the ConfigManager so the
     * change is live for the systems that read it.
     */
    protected void setConfig(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        plugin.getConfigManager().reload();
    }
}
