package com.kitsunetech.fishing.data;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages loading and saving of player fishing data.
 */
public class FisherDataManager {

    private final VanillaFishingExpansion plugin;
    private final Map<UUID, FisherData> playerData;
    private final File dataFolder;

    public FisherDataManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Get or create fisher data for a player.
     */
    public FisherData getFisherData(UUID playerId) {
        if (!playerData.containsKey(playerId)) {
            FisherData data = loadPlayerData(playerId);
            playerData.put(playerId, data);
        }
        return playerData.get(playerId);
    }

    /**
     * Load player data from file.
     */
    private FisherData loadPlayerData(UUID playerId) {
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");

        if (!playerFile.exists()) {
            return new FisherData(playerId);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        int level = config.getInt("level", 1);
        int totalFish = config.getInt("totalFishCaught", 0);
        int fishForLevel = config.getInt("fishForCurrentLevel", 0);
        double speedBonus = config.getDouble("permanentSpeedBonus", 0.0);
        double luckBonus = config.getDouble("permanentLuckBonus", 0.0);
        boolean hasLedger = config.getBoolean("hasConsumedLedger", false);

        return new FisherData(playerId, level, totalFish, fishForLevel, speedBonus, luckBonus, hasLedger);
    }

    /**
     * Save a single player's data.
     */
    public void savePlayerData(UUID playerId) {
        FisherData data = playerData.get(playerId);
        if (data == null)
            return;

        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("level", data.getLevel());
        config.set("totalFishCaught", data.getTotalFishCaught());
        config.set("fishForCurrentLevel", data.getFishForCurrentLevel());
        config.set("permanentSpeedBonus", data.getPermanentSpeedBonus());
        config.set("permanentLuckBonus", data.getPermanentLuckBonus());
        config.set("hasConsumedLedger", data.hasConsumedLedger());

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for player: " + playerId);
        }
    }

    /**
     * Save all player data (called on disable).
     */
    public void saveAllData() {
        for (UUID playerId : playerData.keySet()) {
            savePlayerData(playerId);
        }
        plugin.getLogger().info("Saved fishing data for " + playerData.size() + " players.");
    }

    /**
     * Unload player data (e.g., on quit).
     */
    public void unloadPlayer(UUID playerId) {
        savePlayerData(playerId);
        playerData.remove(playerId);
    }
}
