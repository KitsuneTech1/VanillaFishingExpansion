package com.kitsunetech.fishing.listeners;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.data.FisherData;
import com.kitsunetech.fishing.data.FisherDataManager;
import com.kitsunetech.fishing.loot.LootManager;
import com.kitsunetech.fishing.systems.EventManager;
import com.kitsunetech.fishing.systems.FishJournal;
import com.kitsunetech.fishing.systems.StreakManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

/**
 * Main fishing event listener. Handles XP gain, loot modification, and speed
 * bonuses.
 */
public class FishingListener implements Listener {

    private final VanillaFishingExpansion plugin;
    private final Random random;

    public FishingListener(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        FisherDataManager dataManager = plugin.getFisherDataManager();
        FisherData data = dataManager.getFisherData(player.getUniqueId());

        // Handle catching fish
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            handleCaughtFish(event, player, data);
        }

        // A failed attempt (the fish got away) breaks the catch streak.
        if (event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {
            plugin.getStreakManager().resetStreak(player.getUniqueId());
        }

        // Handle fishing rod cast (for speed modification)
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            applySpeedBonus(event, data);
        }
    }

    /**
     * Handle fish catch: add XP, check for custom loot, durability save.
     */
    private void handleCaughtFish(PlayerFishEvent event, Player player, FisherData data) {
        // Add fish and check for level up (silent to player)
        boolean leveledUp = data.addFish();

        // Update the catch streak (drives "Fishing Frenzy") and gather seasonal-event
        // multipliers. These fold together into the XP and loot-quality bonuses below.
        StreakManager.StreakResult streak = plugin.getStreakManager().recordCatch(player);
        EventManager events = plugin.getEventManager();
        double eventXpMultiplier = events.getXpMultiplier(player);
        double eventLootMultiplier = events.getLootMultiplier(player);

        // Apply the combined XP multiplier to the vanilla fishing XP drop so frenzy and
        // Double-XP / event days actually change the reward.
        double xpMultiplier = streak.xpMultiplier * eventXpMultiplier;
        if (xpMultiplier != 1.0) {
            event.setExpToDrop((int) Math.round(event.getExpToDrop() * xpMultiplier));
        }

        // Log level-up to server console and notify player
        if (leveledUp) {
            int newLevel = data.getLevel();
            int fishForNextLevel = FisherData.getFishRequiredForLevel(newLevel);

            plugin.getLogger().info("[FISHING LEVEL UP] " + player.getName() +
                    " reached Fishing Level " + newLevel +
                    " (Total fish: " + data.getTotalFishCaught() + ")");

            // Notify the player with level up message and fish needed for next level
            if (newLevel < 100) {
                String message = ChatColor.GOLD + "" + ChatColor.BOLD + "⚓ LEVEL UP! " + ChatColor.RESET +
                        ChatColor.YELLOW + "You are now Fishing Level " + ChatColor.AQUA + newLevel +
                        ChatColor.YELLOW + "! Catch " + ChatColor.GREEN + fishForNextLevel +
                        ChatColor.YELLOW + " more fish to reach Level " + ChatColor.AQUA + (newLevel + 1) +
                        ChatColor.YELLOW + "!";
                player.sendMessage(message);
            } else {
                String message = ChatColor.GOLD + "" + ChatColor.BOLD + "⚓ MAX LEVEL! " + ChatColor.RESET +
                        ChatColor.YELLOW + "You have reached Fishing Level " + ChatColor.AQUA + "100" +
                        ChatColor.YELLOW + "! You are a Master Angler!";
                player.sendMessage(message);
            }

            // Play level up sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Save immediately on level-up to prevent data loss
            saveDataAsync(player.getUniqueId());
        }

        // Try to generate custom loot, boosted by the streak-frenzy and event loot bonuses.
        LootManager lootManager = plugin.getLootManager();
        double lootMultiplier = streak.lootMultiplier * eventLootMultiplier;
        ItemStack customLoot = lootManager.generateLoot(player, data, lootMultiplier);

        if (customLoot != null) {
            // Replace vanilla loot with custom loot
            if (event.getCaught() instanceof Item) {
                Item caughtItem = (Item) event.getCaught();
                caughtItem.setItemStack(customLoot);

                // Play subtle effect for rare catches
                playRareCatchEffect(player, customLoot);
            }
        }

        // Record the catch in the player's Fish Journal (final item after any custom
        // loot replacement), grouped by the biome the player is fishing in.
        if (event.getCaught() instanceof Item caughtItem) {
            Material caughtType = caughtItem.getItemStack().getType();
            Biome biome = player.getLocation().getBlock().getBiome();
            String biomeGroup = FishJournal.getBiomeGroup(biome);
            plugin.getFishJournal().recordCatch(player.getUniqueId(), caughtType, biomeGroup);
        }

        // Save data periodically (every 10 catches) as backup
        if (data.getTotalFishCaught() % 10 == 0 && !leveledUp) {
            saveDataAsync(player.getUniqueId());
        }
    }

    /**
     * Save a player's fishing data on an async task.
     */
    private void saveDataAsync(UUID playerId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getFisherDataManager().savePlayerData(playerId);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Apply speed bonus by reducing wait time.
     */
    private void applySpeedBonus(PlayerFishEvent event, FisherData data) {
        double speedReduction = data.getTotalSpeedBonus();

        if (speedReduction > 0 && event.getHook() != null) {
            // Reduce the min/max wait time based on level bonus
            // Vanilla wait time is 5-30 seconds (100-600 ticks)
            int minWait = (int) (100 * (1 - speedReduction));
            int maxWait = (int) (600 * (1 - speedReduction));

            // Ensure minimum values
            minWait = Math.max(20, minWait); // At least 1 second
            maxWait = Math.max(60, maxWait); // At least 3 seconds

            event.getHook().setMinWaitTime(minWait);
            event.getHook().setMaxWaitTime(maxWait);
        }
    }

    /**
     * Play subtle visual/audio effect for rare catches.
     */
    private void playRareCatchEffect(Player player, ItemStack item) {
        String itemName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";

        // Check if it's an ultra-rare or rare custom item
        if (itemName.contains("ULTRA RARE") || itemName.contains("Abyssal") || itemName.contains("Neptune")) {
            // Big effect for ultra-rares
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5,
                    0.5, 0.1);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        } else if (itemName.contains("RARE") || itemName.contains("Sea-Touched") || itemName.contains("Angler")) {
            // Medium effect for rares
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3,
                    0.5);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        } else if (itemName.contains("Shard") || itemName.contains("Crate")) {
            // Small effect for shards and crates
            player.getWorld().spawnParticle(Particle.BUBBLE_POP, player.getLocation().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2,
                    0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.2f);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        // Update leaderboard with final stats before saving
        FisherData data = plugin.getFisherDataManager().getFisherData(playerId);
        plugin.getLeaderboardManager().updatePlayer(
                playerId,
                event.getPlayer().getName(),
                data.getLevel(),
                data.getTotalFishCaught());

        // Save and unload player data on disconnect
        plugin.getFisherDataManager().unloadPlayer(playerId);

        // Clean up passive effect tracking
        plugin.getPassiveEffectListener().removePlayer(playerId);

        // Clean up streak data (memory leak prevention)
        plugin.getStreakManager().resetStreak(playerId);

        // Save and unload fish journal (memory leak prevention)
        plugin.getFishJournal().unloadPlayer(playerId);
    }
}
