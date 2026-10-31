package com.kitsunetech.fishing.commands;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.data.FisherData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Admin command to view fishing statistics.
 */
public class FishingStatsCommand implements CommandExecutor {

    private final VanillaFishingExpansion plugin;

    public FishingStatsCommand(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /fishingstats <player>");
            return true;
        }

        FisherData data = plugin.getFisherDataManager().getFisherData(target.getUniqueId());

        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "═══ " + ChatColor.WHITE + ChatColor.BOLD + "Fishing Stats: " +
                target.getName() + ChatColor.AQUA + " ═══");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "  Level: " + ChatColor.GREEN + data.getLevel() + ChatColor.GRAY + "/100");
        sender.sendMessage(ChatColor.GRAY + "  Total Fish: " + ChatColor.YELLOW + data.getTotalFishCaught());
        sender.sendMessage(ChatColor.GRAY + "  Progress: " + ChatColor.WHITE + data.getFishForCurrentLevel() +
                "/" + FisherData.getFishRequiredForLevel(data.getLevel()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "  Quality Bonus: " + ChatColor.GREEN +
                String.format("+%.1f%%", (data.getQualityMultiplier() - 1) * 100));
        sender.sendMessage(ChatColor.GRAY + "  Speed Bonus: " + ChatColor.GREEN +
                String.format("-%.1f%%", data.getTotalSpeedBonus() * 100) + ChatColor.GRAY + " wait time");
        sender.sendMessage(ChatColor.GRAY + "  Durability Save: " + ChatColor.GREEN +
                String.format("%.1f%%", data.getDurabilitySaveChance() * 100));
        sender.sendMessage("");

        if (data.getPermanentSpeedBonus() > 0 || data.getPermanentLuckBonus() > 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "  ✧ Permanent Buffs:");
            if (data.getPermanentSpeedBonus() > 0) {
                sender.sendMessage(ChatColor.GRAY + "    Speed: " + ChatColor.GREEN +
                        String.format("+%.0f%%", data.getPermanentSpeedBonus() * 100));
            }
            if (data.getPermanentLuckBonus() > 0) {
                sender.sendMessage(ChatColor.GRAY + "    Luck: " + ChatColor.GREEN +
                        String.format("+%.0f%%", data.getPermanentLuckBonus() * 100));
            }
            sender.sendMessage("");
        }

        sender.sendMessage(ChatColor.AQUA + "═══════════════════════════");

        return true;
    }
}
