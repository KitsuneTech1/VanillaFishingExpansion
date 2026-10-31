package com.kitsunetech.fishing.commands;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.systems.FishJournal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * /fishjournal command - Shows fish collection progress.
 */
public class FishJournalCommand implements CommandExecutor {

    private final VanillaFishingExpansion plugin;

    public FishJournalCommand(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!plugin.getConfigManager().isFishJournalEnabled()) {
            sender.sendMessage(ChatColor.RED + "Fish Journal is disabled.");
            return true;
        }

        Player player = (Player) sender;
        FishJournal journal = plugin.getFishJournal();

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "═══ " + ChatColor.WHITE + ChatColor.BOLD +
                "📖 Fish Journal 📖" + ChatColor.AQUA + " ═══");
        player.sendMessage("");

        // Total progress
        double totalProgress = journal.getTotalProgress(player.getUniqueId()) * 100;
        player.sendMessage(ChatColor.WHITE + "Total Completion: " + getProgressBar(totalProgress) +
                ChatColor.YELLOW + String.format(" %.1f%%", totalProgress));
        player.sendMessage("");

        // Per-biome progress
        Set<String> biomes = FishJournal.getAllBiomeGroups();
        for (String biome : biomes) {
            double progress = journal.getBiomeProgress(player.getUniqueId(), biome) * 100;
            boolean complete = journal.isBiomeComplete(player.getUniqueId(), biome);

            String biomeName = capitalize(biome);
            String status = complete ? ChatColor.GREEN + "✓ COMPLETE" : getProgressBar(progress);

            player.sendMessage(ChatColor.GRAY + "  " + biomeName + ": " + status +
                    (complete ? "" : ChatColor.GRAY + String.format(" %.0f%%", progress)));
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════");

        return true;
    }

    private String getProgressBar(double percent) {
        int filled = (int) (percent / 10);
        int empty = 10 - filled;

        StringBuilder bar = new StringBuilder(ChatColor.GREEN + "");
        for (int i = 0; i < filled; i++)
            bar.append("█");
        bar.append(ChatColor.GRAY);
        for (int i = 0; i < empty; i++)
            bar.append("░");

        return bar.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
