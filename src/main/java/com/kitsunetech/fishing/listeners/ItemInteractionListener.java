package com.kitsunetech.fishing.listeners;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.data.FisherData;
import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Handles custom item interactions (consuming books, opening crates).
 */
public class ItemInteractionListener implements Listener {

    private final VanillaFishingExpansion plugin;
    private final Random random;

    public ItemInteractionListener(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        String customId = CustomItems.getCustomItemId(item);
        if (customId == null)
            return;

        Player player = event.getPlayer();

        switch (customId) {
            case CustomItems.NEPTUNES_LEDGER:
                consumeNeptunesLedger(player, item);
                event.setCancelled(true);
                break;

            case CustomItems.ANGLERS_ALMANAC:
                consumeAnglersAlmanac(player, item);
                event.setCancelled(true);
                break;

            case CustomItems.SUNKEN_CRATE:
                openSunkenCrate(player, item);
                event.setCancelled(true);
                break;
        }
    }

    /**
     * Consume Neptune's Ledger: +10% speed, +5% luck permanently.
     */
    private void consumeNeptunesLedger(Player player, ItemStack item) {
        FisherData data = plugin.getFisherDataManager().getFisherData(player.getUniqueId());

        // Add permanent bonuses
        data.addPermanentSpeedBonus(0.10); // +10%
        data.addPermanentLuckBonus(0.05); // +5%
        data.setConsumedLedger(true); // Unlock Elder Fisherman particles

        // Consume the item
        item.setAmount(item.getAmount() - 1);

        // Play epic effect
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        // Send message
        player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.WHITE + "You absorbed the knowledge of " +
                ChatColor.GOLD + "Neptune's Ledger" + ChatColor.WHITE + "!");
        player.sendMessage(ChatColor.GREEN + "  +10% Fishing Speed " + ChatColor.GRAY + "(Permanent)");
        player.sendMessage(ChatColor.GREEN + "  +5% Luck " + ChatColor.GRAY + "(Permanent)");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ✧ Elder Fisherman particles unlocked!");

        // Save immediately
        plugin.getFisherDataManager().savePlayerData(player.getUniqueId());
    }

    /**
     * Consume Angler's Almanac: +3% speed, +1% luck permanently.
     */
    private void consumeAnglersAlmanac(Player player, ItemStack item) {
        FisherData data = plugin.getFisherDataManager().getFisherData(player.getUniqueId());

        // Add permanent bonuses
        data.addPermanentSpeedBonus(0.03); // +3%
        data.addPermanentLuckBonus(0.01); // +1%

        // Consume the item
        item.setAmount(item.getAmount() - 1);

        // Play effect
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.5);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        // Send message
        player.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.WHITE + "You studied the " +
                ChatColor.GREEN + "Angler's Almanac" + ChatColor.WHITE + "!");
        player.sendMessage(ChatColor.GREEN + "  +3% Fishing Speed " + ChatColor.GRAY + "(Permanent)");
        player.sendMessage(ChatColor.GREEN + "  +1% Luck " + ChatColor.GRAY + "(Permanent)");

        // Save immediately
        plugin.getFisherDataManager().savePlayerData(player.getUniqueId());
    }

    /**
     * Open Sunken Crate: Random treasure drops.
     */
    private void openSunkenCrate(Player player, ItemStack item) {
        // Consume the crate
        item.setAmount(item.getAmount() - 1);

        // Play opening effect
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

        player.sendMessage(ChatColor.DARK_GREEN + "✦ Opening " + ChatColor.BOLD + "Sunken Crate" + ChatColor.RESET
                + ChatColor.DARK_GREEN + "...");

        // Generate random loot (3-5 items)
        int lootCount = random.nextInt(3) + 3;

        for (int i = 0; i < lootCount; i++) {
            ItemStack loot = generateCrateLoot();

            // Give to player (drop if inventory full)
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), loot);
            } else {
                player.getInventory().addItem(loot);
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
    }

    /**
     * Generate random loot from a crate.
     */
    private ItemStack generateCrateLoot() {
        int roll = random.nextInt(100);

        if (roll < 30) { // 30% Iron
            return new ItemStack(Material.IRON_INGOT, random.nextInt(8) + 4);
        } else if (roll < 55) { // 25% Gold
            return new ItemStack(Material.GOLD_INGOT, random.nextInt(6) + 3);
        } else if (roll < 75) { // 20% Emeralds
            return new ItemStack(Material.EMERALD, random.nextInt(5) + 2);
        } else if (roll < 90) { // 15% Diamonds
            return new ItemStack(Material.DIAMOND, random.nextInt(3) + 1);
        } else if (roll < 97) { // 7% Lapis
            return new ItemStack(Material.LAPIS_LAZULI, random.nextInt(16) + 8);
        } else { // 3% Netherite Scrap
            return new ItemStack(Material.NETHERITE_SCRAP);
        }
    }
}
