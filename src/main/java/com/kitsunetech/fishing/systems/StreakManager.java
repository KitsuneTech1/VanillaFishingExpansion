package com.kitsunetech.fishing.systems;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks fishing streaks and "Fishing Frenzy" mode.
 */
public class StreakManager {

    private final VanillaFishingExpansion plugin;
    private final Map<UUID, StreakData> playerStreaks;

    public StreakManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.playerStreaks = new HashMap<>();
    }

    /**
     * Record a fish catch and update streak.
     * Returns the current streak multipliers.
     */
    public StreakResult recordCatch(Player player) {
        UUID playerId = player.getUniqueId();
        ConfigManager config = plugin.getConfigManager();

        if (!config.isStreakSystemEnabled()) {
            return new StreakResult(1, 1.0, 1.0, false);
        }

        long now = System.currentTimeMillis();
        StreakData data = playerStreaks.get(playerId);

        if (data == null) {
            data = new StreakData();
            playerStreaks.put(playerId, data);
        }

        // Check if streak timed out
        long timeout = config.getStreakTimeout() * 1000L;
        if (now - data.lastCatch > timeout) {
            data.streak = 0;
        }

        // Increment streak
        data.streak++;
        data.lastCatch = now;

        boolean wasFrenzy = data.inFrenzy;
        data.inFrenzy = data.streak >= config.getFrenzyThreshold();

        // Just activated frenzy?
        if (data.inFrenzy && !wasFrenzy) {
            activateFrenzy(player, data.streak);
        }

        // Show streak milestones
        if (data.streak == 5) {
            player.sendMessage(ChatColor.AQUA + "🔥 " + ChatColor.WHITE + "5 catch streak!");
        }

        double xpMult = data.inFrenzy ? config.getFrenzyXpMultiplier() : 1.0;
        double lootMult = data.inFrenzy ? config.getFrenzyLootBonus() : 1.0;

        return new StreakResult(data.streak, xpMult, lootMult, data.inFrenzy);
    }

    /**
     * Activate Fishing Frenzy mode for a player.
     */
    private void activateFrenzy(Player player, int streak) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.BOLD + "FISHING FRENZY! " +
                ChatColor.RESET + ChatColor.GOLD + "✦");
        player.sendMessage(ChatColor.YELLOW + "  " + streak + " catches in a row!");
        player.sendMessage(ChatColor.GREEN + "  2x XP • 1.5x Loot Quality");
        player.sendMessage("");

        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        if (plugin.getConfigManager().isLogFrenzyActivations()) {
            plugin.getLogger().info("[FISHING FRENZY] " + player.getName() +
                    " activated frenzy at " + streak + " catches!");
        }
    }

    /**
     * Get current streak for a player.
     */
    public int getStreak(UUID playerId) {
        StreakData data = playerStreaks.get(playerId);
        return data != null ? data.streak : 0;
    }

    /**
     * Check if player is in frenzy mode.
     */
    public boolean isInFrenzy(UUID playerId) {
        StreakData data = playerStreaks.get(playerId);
        return data != null && data.inFrenzy;
    }

    /**
     * Reset streak for a player.
     */
    public void resetStreak(UUID playerId) {
        playerStreaks.remove(playerId);
    }

    /**
     * Internal streak tracking data.
     */
    private static class StreakData {
        int streak = 0;
        long lastCatch = 0;
        boolean inFrenzy = false;
    }

    /**
     * Result of recording a catch, includes multipliers.
     */
    public static class StreakResult {
        public final int streak;
        public final double xpMultiplier;
        public final double lootMultiplier;
        public final boolean inFrenzy;

        public StreakResult(int streak, double xpMult, double lootMult, boolean inFrenzy) {
            this.streak = streak;
            this.xpMultiplier = xpMult;
            this.lootMultiplier = lootMult;
            this.inFrenzy = inFrenzy;
        }
    }
}
