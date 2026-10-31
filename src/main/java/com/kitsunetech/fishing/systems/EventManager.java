package com.kitsunetech.fishing.systems;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Random;

/**
 * Manages seasonal events: Double XP, Rain bonus, Blood Moon.
 */
public class EventManager {

    private final VanillaFishingExpansion plugin;
    private final Random random;

    public EventManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Check if today is a Double XP day.
     */
    public boolean isDoubleXpDay() {
        ConfigManager config = plugin.getConfigManager();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return config.getDoubleXpDays().contains(today);
    }

    /**
     * Check if it's raining in the player's world.
     */
    public boolean isRaining(Player player) {
        return player.getWorld().hasStorm();
    }

    /**
     * Check if it's currently a Blood Moon (night + full moon).
     */
    public boolean isBloodMoon(Player player) {
        if (!plugin.getConfigManager().isBloodMoonEnabled())
            return false;

        World world = player.getWorld();
        long time = world.getTime();

        // Night time: 13000-23000 ticks
        boolean isNight = time >= 13000 && time <= 23000;

        // Full moon: day 0, 8, 16, 24... (every 8 days)
        long days = world.getFullTime() / 24000;
        boolean isFullMoon = (days % 8) == 0;

        return isNight && isFullMoon;
    }

    /**
     * Get total XP multiplier from events.
     */
    public double getXpMultiplier(Player player) {
        double mult = 1.0;
        if (isDoubleXpDay())
            mult *= 2.0;
        return mult;
    }

    /**
     * Get total loot quality multiplier from events.
     */
    public double getLootMultiplier(Player player) {
        ConfigManager config = plugin.getConfigManager();
        double mult = 1.0;

        if (isRaining(player)) {
            mult *= config.getRainBonus();
        }

        if (isBloodMoon(player)) {
            mult *= config.getBloodMoonBonus();
        }

        return mult;
    }

    /**
     * Roll for Blood Moon special drops.
     * Returns null if no special drop.
     */
    public ItemStack rollBloodMoonDrop(Player player) {
        if (!isBloodMoon(player))
            return null;

        ConfigManager config = plugin.getConfigManager();

        // 5% chance for blood moon drops
        if (random.nextDouble() < 0.05) {
            var drops = config.getBloodMoonDrops();
            if (!drops.isEmpty()) {
                Material mat = drops.get(random.nextInt(drops.size()));
                return new ItemStack(mat);
            }
        }

        return null;
    }

    /**
     * Get event status text for display.
     */
    public String getActiveEventsText(Player player) {
        StringBuilder sb = new StringBuilder();

        if (isDoubleXpDay()) {
            sb.append("§e✦ Double XP Weekend §7| ");
        }
        if (isRaining(player)) {
            sb.append("§b☔ Rain Bonus §7| ");
        }
        if (isBloodMoon(player)) {
            sb.append("§c🌙 Blood Moon §7| ");
        }

        if (sb.length() == 0) {
            return "§7No active events";
        }

        // Remove trailing " | "
        return sb.substring(0, sb.length() - 3);
    }
}
