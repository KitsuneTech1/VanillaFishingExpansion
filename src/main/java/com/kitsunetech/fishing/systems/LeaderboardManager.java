package com.kitsunetech.fishing.systems;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages fishing leaderboard.
 */
public class LeaderboardManager {

    private final VanillaFishingExpansion plugin;
    private final File leaderboardFile;
    private final Map<UUID, LeaderboardEntry> entries;
    private List<LeaderboardEntry> sortedLeaderboard;
    private UUID currentLeader;

    public LeaderboardManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.leaderboardFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        this.entries = new HashMap<>();
        this.sortedLeaderboard = new ArrayList<>();
        load();
    }

    /**
     * Update a player's stats on the leaderboard.
     */
    public void updatePlayer(UUID playerId, String playerName, int level, int totalFish) {
        if (!plugin.getConfigManager().isLeaderboardEnabled())
            return;

        LeaderboardEntry entry = entries.get(playerId);
        if (entry == null) {
            entry = new LeaderboardEntry(playerId, playerName, level, totalFish);
            entries.put(playerId, entry);
        } else {
            entry.playerName = playerName;
            entry.level = level;
            entry.totalFish = totalFish;
        }

        // Resort leaderboard
        sortLeaderboard();

        // Check for new #1
        if (plugin.getConfigManager().isBroadcastNewLeader()) {
            if (!sortedLeaderboard.isEmpty()) {
                UUID newLeader = sortedLeaderboard.get(0).playerId;
                if (currentLeader != null && !currentLeader.equals(newLeader) && newLeader.equals(playerId)) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "🏆 " + ChatColor.WHITE + playerName +
                            ChatColor.GOLD + " is now the #1 Fisher! " + ChatColor.GRAY +
                            "(Level " + level + ", " + totalFish + " fish)");
                }
                currentLeader = newLeader;
            }
        }
    }

    /**
     * Get the top N players.
     */
    public List<LeaderboardEntry> getTop(int count) {
        if (count <= 0 || sortedLeaderboard.isEmpty()) {
            return List.of();
        }
        int limit = Math.min(count, sortedLeaderboard.size());
        return List.copyOf(sortedLeaderboard.subList(0, limit));
    }

    /**
     * Get a player's rank (1-indexed, 0 if not found).
     */
    public int getRank(UUID playerId) {
        for (int i = 0; i < sortedLeaderboard.size(); i++) {
            if (sortedLeaderboard.get(i).playerId.equals(playerId)) {
                return i + 1;
            }
        }
        return 0;
    }

    private void sortLeaderboard() {
        sortedLeaderboard = new ArrayList<>(entries.values());
        sortedLeaderboard.sort((a, b) -> {
            if (b.level != a.level)
                return Integer.compare(b.level, a.level);
            return Integer.compare(b.totalFish, a.totalFish);
        });
    }

    /**
     * Load leaderboard from file.
     */
    private void load() {
        if (!leaderboardFile.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(leaderboardFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                String name = config.getString(key + ".name", "Unknown");
                int level = config.getInt(key + ".level", 1);
                int fish = config.getInt(key + ".fish", 0);
                entries.put(playerId, new LeaderboardEntry(playerId, name, level, fish));
            } catch (IllegalArgumentException ignored) {
                // Key in leaderboard.yml is not a valid UUID, skip it
            }
        }
        sortLeaderboard();
        if (!sortedLeaderboard.isEmpty()) {
            currentLeader = sortedLeaderboard.get(0).playerId;
        }
    }

    /**
     * Save leaderboard to file.
     */
    public void save() {
        FileConfiguration config = new YamlConfiguration();
        for (LeaderboardEntry entry : entries.values()) {
            String key = entry.playerId.toString();
            config.set(key + ".name", entry.playerName);
            config.set(key + ".level", entry.level);
            config.set(key + ".fish", entry.totalFish);
        }
        try {
            config.save(leaderboardFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save leaderboard: " + e.getMessage());
        }
    }

    /**
     * Leaderboard entry data.
     */
    public static class LeaderboardEntry {
        public final UUID playerId;
        public String playerName;
        public int level;
        public int totalFish;

        public LeaderboardEntry(UUID playerId, String playerName, int level, int totalFish) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.level = level;
            this.totalFish = totalFish;
        }
    }
}
