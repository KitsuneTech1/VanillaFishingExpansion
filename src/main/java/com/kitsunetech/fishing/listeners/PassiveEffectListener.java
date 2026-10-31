package com.kitsunetech.fishing.listeners;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles passive effects from holding custom items (Abyssal Pearl, Sea-Touched
 * Shell).
 */
public class PassiveEffectListener implements Listener {

    private final VanillaFishingExpansion plugin;
    private final Set<UUID> playersWithAbyssalPearl;
    private final Set<UUID> playersWithSeaShell;

    public PassiveEffectListener(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.playersWithAbyssalPearl = new HashSet<>();
        this.playersWithSeaShell = new HashSet<>();

        // Start passive effect task
        startPassiveEffectTask();
    }

    /**
     * Check for held items when player switches items.
     */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        checkPassiveItems(event.getPlayer());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        // Delay check to account for the swap
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPassiveItems(event.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Check if player is holding passive items.
     */
    private void checkPassiveItems(Player player) {
        UUID playerId = player.getUniqueId();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Check for Abyssal Pearl
        boolean hasAbyssal = isAbyssalPearl(mainHand) || isAbyssalPearl(offHand);
        if (hasAbyssal) {
            playersWithAbyssalPearl.add(playerId);
        } else {
            playersWithAbyssalPearl.remove(playerId);
        }

        // Check for Sea-Touched Shell
        boolean hasShell = isSeaTouchedShell(mainHand) || isSeaTouchedShell(offHand);
        if (hasShell) {
            playersWithSeaShell.add(playerId);
        } else {
            playersWithSeaShell.remove(playerId);
        }
    }

    private boolean isAbyssalPearl(ItemStack item) {
        String id = CustomItems.getCustomItemId(item);
        return CustomItems.ABYSSAL_PEARL.equals(id);
    }

    private boolean isSeaTouchedShell(ItemStack item) {
        String id = CustomItems.getCustomItemId(item);
        return CustomItems.SEA_TOUCHED_SHELL.equals(id);
    }

    /**
     * Apply passive effects every second.
     */
    private void startPassiveEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Apply Abyssal Pearl effects (+40% swim speed, dolphin's grace, water
                // breathing)
                for (UUID playerId : playersWithAbyssalPearl) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        // Dolphin's Grace
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false, false));
                        // Water Breathing
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false, false));

                        // Swim speed is handled via attribute modifier or Speed effect in water
                        if (player.isInWater()) {
                            // Level 1 Speed in water = ~40% boost (0.2 base * 1.2 = ~20% per level)
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, false));
                        }
                    }
                }

                // Apply Sea-Touched Shell effects (+15% swim speed, water breathing)
                for (UUID playerId : playersWithSeaShell) {
                    // Skip if already has Abyssal Pearl (better version)
                    if (playersWithAbyssalPearl.contains(playerId))
                        continue;

                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        // Water Breathing
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false, false));

                        // Swim speed (+15%)
                        if (player.isInWater()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
                        }
                    }
                }

                // Elder Fisherman particles for players who consumed Neptune's Ledger
                if (plugin.getConfigManager().isElderParticlesEnabled()) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        var data = plugin.getFisherDataManager().getFisherData(player.getUniqueId());
                        if (data.hasConsumedLedger()) {
                            // Subtle ambient particles around the player
                            player.getWorld().spawnParticle(
                                    org.bukkit.Particle.ENCHANT,
                                    player.getLocation().add(0, 1.5, 0),
                                    3, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Every second
    }

    /**
     * Clean up when player leaves.
     */
    public void removePlayer(UUID playerId) {
        playersWithAbyssalPearl.remove(playerId);
        playersWithSeaShell.remove(playerId);
    }
}
