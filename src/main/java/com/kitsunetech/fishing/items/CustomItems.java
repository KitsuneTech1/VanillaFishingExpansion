package com.kitsunetech.fishing.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * Factory for creating custom fishing items.
 */
public class CustomItems {

    public static final NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey("vanillafishing", "custom_item");

    // Custom item identifiers
    public static final String ABYSSAL_PEARL = "abyssal_pearl";
    public static final String NEPTUNES_LEDGER = "neptunes_ledger";
    public static final String SEA_TOUCHED_SHELL = "sea_touched_shell";
    public static final String ANGLERS_ALMANAC = "anglers_almanac";
    public static final String TRIDENT_SHARD = "trident_shard";
    public static final String TOTEM_SHARD = "totem_shard";
    public static final String SUNKEN_CRATE = "sunken_crate";
    public static final String ANGLERS_ROD = "anglers_rod";

    /**
     * The Abyssal Pearl - Ultra Rare
     * Passive: +40% Swim Speed, Dolphin's Grace, Water Breathing while held.
     */
    public static ItemStack createAbyssalPearl() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "The Abyssal Pearl");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "An ancient pearl from the deepest trenches.",
                "",
                ChatColor.AQUA + "✦ Passive (Main/Off-hand):",
                ChatColor.WHITE + "  • Dolphin's Grace",
                ChatColor.WHITE + "  • Water Breathing",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+40% Swim Speed",
                "",
                ChatColor.LIGHT_PURPLE + "✧ ULTRA RARE"));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, ABYSSAL_PEARL);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Neptune's Ledger - Ultra Rare
     * Consumable: +10% Fishing Speed, +5% Luck permanently.
     */
    public static ItemStack createNeptunesLedger() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Neptune's Ledger");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "The sea god's personal fishing records.",
                "",
                ChatColor.YELLOW + "✦ Right-Click to Consume:",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+10% Fishing Speed" + ChatColor.WHITE + " (Permanent)",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+5% Luck" + ChatColor.WHITE + " (Permanent)",
                ChatColor.WHITE + "  • Unlocks 'Elder Fisherman' particles",
                "",
                ChatColor.LIGHT_PURPLE + "✧ ULTRA RARE"));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, NEPTUNES_LEDGER);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Sea-Touched Shell - Rare
     * Passive: +15% Swim Speed, Water Breathing while held.
     */
    public static ItemStack createSeaTouchedShell() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Sea-Touched Shell");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A shell blessed by ocean currents.",
                "",
                ChatColor.AQUA + "✦ Passive (Main/Off-hand):",
                ChatColor.WHITE + "  • Water Breathing",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+15% Swim Speed",
                "",
                ChatColor.BLUE + "✦ RARE"));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, SEA_TOUCHED_SHELL);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Angler's Almanac - Rare
     * Consumable: +3% Fishing Speed, +1% Luck permanently.
     */
    public static ItemStack createAnglersAlmanac() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Angler's Almanac");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A worn journal of fishing secrets.",
                "",
                ChatColor.YELLOW + "✦ Right-Click to Consume:",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+3% Fishing Speed" + ChatColor.WHITE + " (Permanent)",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+1% Luck" + ChatColor.WHITE + " (Permanent)",
                "",
                ChatColor.BLUE + "✦ RARE"));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, ANGLERS_ALMANAC);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Trident Shard - Collect 9 to craft a Trident.
     */
    public static ItemStack createTridentShard() {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_AQUA + "Trident Shard");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A fragment of a broken trident.",
                "",
                ChatColor.YELLOW + "Collect 9 to craft a Trident!"));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, TRIDENT_SHARD);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Totem Shard - Collect 4 to craft a Totem of Undying.
     */
    public static ItemStack createTotemShard() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Totem Shard");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A fragment of an ancient totem.",
                "",
                ChatColor.YELLOW + "Collect 4 to craft a Totem of Undying!"));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, TOTEM_SHARD);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Sunken Crate - Contains randomized treasure.
     */
    public static ItemStack createSunkenCrate() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Sunken Crate");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A mysterious crate from the depths.",
                "",
                ChatColor.YELLOW + "Right-Click to open!",
                ChatColor.WHITE + "Contains: Iron, Gold, Emeralds, Diamonds..."));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, SUNKEN_CRATE);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Angler's Rod - Rare custom fishing rod
     * Passive: +5% catch rate when fishing.
     */
    public static ItemStack createAnglersRod() {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Angler's Rod");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A finely crafted rod from a master angler.",
                "",
                ChatColor.AQUA + "✦ Passive Bonus:",
                ChatColor.WHITE + "  • " + ChatColor.GREEN + "+5% Catch Rate",
                ChatColor.WHITE + "  • " + ChatColor.YELLOW + "Cannot be crafted",
                "",
                ChatColor.BLUE + "✦ RARE"));

        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, ANGLERS_ROD);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if an item is a custom fishing item.
     */
    public static String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(CUSTOM_ITEM_KEY, PersistentDataType.STRING))
            return null;
        return meta.getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING);
    }
}
