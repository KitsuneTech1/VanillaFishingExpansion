package com.kitsunetech.fishing.config;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Manages plugin configuration.
 */
public class ConfigManager {

    private final VanillaFishingExpansion plugin;
    private FileConfiguration config;

    // Leveling
    private int baseFishRequired;
    private double scalingMultiplier;
    private int maxLevel;

    // Drop chances
    private double ultraRareBase;
    private double rareBase;
    private double uncommonBase;
    private double commonBase;

    // Features
    private boolean elderParticles;
    private boolean streakSystem;
    private boolean anglersRod;
    private boolean fishJournal;
    private boolean leaderboard;
    private boolean netherFishing;
    private int netherFishingLevel;

    // Streak
    private int streakTimeout;
    private int frenzyThreshold;
    private double frenzyXpMultiplier;
    private double frenzyLootBonus;

    // Events
    private double rainBonus;
    private Set<DayOfWeek> doubleXpDays;
    private boolean bloodMoonEnabled;
    private double bloodMoonBonus;
    private List<Material> bloodMoonDrops;

    // Leaderboard
    private int topCount;
    private boolean broadcastNewLeader;

    // Journal
    private double biomeCompleteBonus;
    private double fullCompleteBonus;

    // Angler's Rod
    private double anglersRodCatchBonus;
    private double anglersRodDropChance;

    // Logging
    private boolean logLevelUps;
    private boolean logRareCatches;
    private boolean logFrenzyActivations;

    public ConfigManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Leveling
        baseFishRequired = config.getInt("leveling.base-fish-required", 10);
        scalingMultiplier = config.getDouble("leveling.scaling-multiplier", 1.025);
        maxLevel = config.getInt("leveling.max-level", 100);

        // Drops
        ultraRareBase = config.getDouble("drops.ultra-rare-base", 0.0001);
        rareBase = config.getDouble("drops.rare-base", 0.001);
        uncommonBase = config.getDouble("drops.uncommon-base", 0.02);
        commonBase = config.getDouble("drops.common-base", 0.1);

        // Features
        elderParticles = config.getBoolean("features.elder-particles", true);
        streakSystem = config.getBoolean("features.streak-system", true);
        anglersRod = config.getBoolean("features.anglers-rod", true);
        fishJournal = config.getBoolean("features.fish-journal", true);
        leaderboard = config.getBoolean("features.leaderboard", true);
        netherFishing = config.getBoolean("features.nether-fishing", true);
        netherFishingLevel = config.getInt("features.nether-fishing-level", 20);

        // Streak
        streakTimeout = config.getInt("streak.timeout-seconds", 30);
        frenzyThreshold = config.getInt("streak.frenzy-threshold", 10);
        frenzyXpMultiplier = config.getDouble("streak.frenzy-xp-multiplier", 2.0);
        frenzyLootBonus = config.getDouble("streak.frenzy-loot-bonus", 1.5);

        // Events
        rainBonus = config.getDouble("events.rain-bonus", 1.10);
        doubleXpDays = new HashSet<>();
        List<String> dayNames = config.getStringList("events.double-xp-days");
        for (String day : dayNames) {
            if (day == null) {
                continue;
            }
            try {
                doubleXpDays.add(DayOfWeek.valueOf(day.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Not a valid day name in config.yml, skip it
            }
        }
        bloodMoonEnabled = config.getBoolean("events.blood-moon.enabled", true);
        bloodMoonBonus = config.getDouble("events.blood-moon.loot-bonus", 2.0);
        bloodMoonDrops = new ArrayList<>();
        for (String mat : config.getStringList("events.blood-moon.drops")) {
            if (mat == null) {
                continue;
            }
            try {
                bloodMoonDrops.add(Material.valueOf(mat.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Not a valid material name in config.yml, skip it
            }
        }

        // Leaderboard
        topCount = Math.max(0, config.getInt("leaderboard.top-count", 10));
        broadcastNewLeader = config.getBoolean("leaderboard.broadcast-new-leader", true);

        // Journal
        biomeCompleteBonus = config.getDouble("journal.biome-complete-bonus", 1.25);
        fullCompleteBonus = config.getDouble("journal.full-complete-bonus", 1.50);

        // Angler's Rod
        anglersRodCatchBonus = config.getDouble("items.anglers-rod.catch-bonus", 0.05);
        anglersRodDropChance = config.getDouble("items.anglers-rod.drop-chance", 0.002);

        // Logging
        logLevelUps = config.getBoolean("logging.level-ups", true);
        logRareCatches = config.getBoolean("logging.rare-catches", true);
        logFrenzyActivations = config.getBoolean("logging.frenzy-activations", false);
    }

    // Getters
    public int getBaseFishRequired() {
        return baseFishRequired;
    }

    public double getScalingMultiplier() {
        return scalingMultiplier;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getUltraRareBase() {
        return ultraRareBase;
    }

    public double getRareBase() {
        return rareBase;
    }

    public double getUncommonBase() {
        return uncommonBase;
    }

    public double getCommonBase() {
        return commonBase;
    }

    public boolean isElderParticlesEnabled() {
        return elderParticles;
    }

    public boolean isStreakSystemEnabled() {
        return streakSystem;
    }

    public boolean isAnglersRodEnabled() {
        return anglersRod;
    }

    public boolean isFishJournalEnabled() {
        return fishJournal;
    }

    public boolean isLeaderboardEnabled() {
        return leaderboard;
    }

    public boolean isNetherFishingEnabled() {
        return netherFishing;
    }

    public int getNetherFishingLevel() {
        return netherFishingLevel;
    }

    public int getStreakTimeout() {
        return streakTimeout;
    }

    public int getFrenzyThreshold() {
        return frenzyThreshold;
    }

    public double getFrenzyXpMultiplier() {
        return frenzyXpMultiplier;
    }

    public double getFrenzyLootBonus() {
        return frenzyLootBonus;
    }

    public double getRainBonus() {
        return rainBonus;
    }

    public Set<DayOfWeek> getDoubleXpDays() {
        return doubleXpDays;
    }

    public boolean isBloodMoonEnabled() {
        return bloodMoonEnabled;
    }

    public double getBloodMoonBonus() {
        return bloodMoonBonus;
    }

    public List<Material> getBloodMoonDrops() {
        return bloodMoonDrops;
    }

    public int getTopCount() {
        return topCount;
    }

    public boolean isBroadcastNewLeader() {
        return broadcastNewLeader;
    }

    public double getBiomeCompleteBonus() {
        return biomeCompleteBonus;
    }

    public double getFullCompleteBonus() {
        return fullCompleteBonus;
    }

    public double getAnglersRodCatchBonus() {
        return anglersRodCatchBonus;
    }

    public double getAnglersRodDropChance() {
        return anglersRodDropChance;
    }

    public boolean isLogLevelUps() {
        return logLevelUps;
    }

    public boolean isLogRareCatches() {
        return logRareCatches;
    }

    public boolean isLogFrenzyActivations() {
        return logFrenzyActivations;
    }
}
