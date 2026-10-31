package com.kitsunetech.fishing.commands;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.systems.LeaderboardManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /fishtop command - Shows top fishers leaderboard.
 */
public class FishTopCommand implements CommandExecutor {

    private final VanillaFishingExpansion plugin;

    public FishTopCommand(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isLeaderboardEnabled()) {
            sender.sendMessage(ChatColor.RED + "Leaderboard is disabled.");
            return true;
        }

        int count = plugin.getConfigManager().getTopCount();
        List<LeaderboardManager.LeaderboardEntry> top = plugin.getLeaderboardManager().getTop(count);

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "═══ " + ChatColor.WHITE + ChatColor.BOLD +
                "🏆 Top Fishers 🏆" + ChatColor.GOLD + " ═══");
        sender.sendMessage("");

        if (top.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  No data yet! Start fishing!");
        } else {
            for (int i = 0; i < top.size(); i++) {
                LeaderboardManager.LeaderboardEntry entry = top.get(i);
                String rank = getRankPrefix(i + 1);
                sender.sendMessage(rank + " " + ChatColor.WHITE + entry.playerName +
                        ChatColor.GRAY + " - Level " + ChatColor.GREEN + entry.level +
                        ChatColor.GRAY + " (" + ChatColor.YELLOW + entry.totalFish + " fish" + ChatColor.GRAY + ")");
            }
        }

        // Show player's own rank if they're a player
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int myRank = plugin.getLeaderboardManager().getRank(player.getUniqueId());
            if (myRank > 0 && myRank > count) {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.GRAY + "Your rank: " + ChatColor.YELLOW + "#" + myRank);
            }
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════");

        return true;
    }

    private String getRankPrefix(int rank) {
        switch (rank) {
            case 1:
                return ChatColor.GOLD + "🥇 #1";
            case 2:
                return ChatColor.GRAY + "🥈 #2";
            case 3:
                return ChatColor.RED + "🥉 #3";
            default:
                return ChatColor.WHITE + "   #" + rank;
        }
    }
}
