package com.kitsunetech.fishing.loot;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import com.kitsunetech.fishing.data.FisherData;
import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages loot generation based on player level, biome, and weather.
 */
public class LootManager {

    private final VanillaFishingExpansion plugin;
    private final Random random;

    public LootManager(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Generate custom loot based on player's fishing data and environment.
     * Returns null if no custom loot should drop (vanilla loot remains).
     */
    public ItemStack generateLoot(Player player, FisherData data) {
        return generateLoot(player, data, 1.0);
    }

    /**
     * Generate custom loot with an extra quality multiplier applied on top of the
     * player's level-based quality. Used to fold in streak-frenzy and seasonal-event
     * loot bonuses so a higher multiplier means better/more frequent custom drops.
     * Returns null if no custom loot should drop (vanilla loot remains).
     */
    public ItemStack generateLoot(Player player, FisherData data, double extraQualityMultiplier) {
        double qualityMultiplier = data.getQualityMultiplier() * extraQualityMultiplier;
        World world = player.getWorld();
        Biome biome = player.getLocation().getBlock().getBiome();

        ItemStack bloodMoonLoot = plugin.getEventManager().rollBloodMoonDrop(player);
        if (bloodMoonLoot != null) {
            return bloodMoonLoot;
        }

        // Roll for custom items (rarest first)
        ItemStack customLoot = rollForCustomItems(data.getLevel(), qualityMultiplier);
        if (customLoot != null) {
            return customLoot;
        }

        // Roll for biome-specific loot
        ItemStack biomeLoot = rollForBiomeLoot(biome, data.getLevel(), qualityMultiplier, world);
        if (biomeLoot != null) {
            return biomeLoot;
        }

        // Roll for global treasure pool
        ItemStack globalLoot = rollForGlobalTreasure(data.getLevel(), qualityMultiplier);
        if (globalLoot != null) {
            return globalLoot;
        }

        return null; // Let vanilla loot happen
    }

    /**
     * Roll for ultra-rare and rare custom items.
     */
    private ItemStack rollForCustomItems(int level, double qualityMultiplier) {
        double roll = random.nextDouble();

        // Ultra-Rares (available at any level, scales heavily)
        double ultraRareChance = plugin.getConfigManager().getUltraRareBase() * qualityMultiplier
                * (1 + level * 0.02);

        if (roll < ultraRareChance / 2) {
            return CustomItems.createAbyssalPearl();
        } else if (roll < ultraRareChance) {
            return CustomItems.createNeptunesLedger();
        }

        // Rares (more common)
        double rareChance = plugin.getConfigManager().getRareBase() * qualityMultiplier * (1 + level * 0.01);
        roll = random.nextDouble();

        if (roll < rareChance / 3) {
            return CustomItems.createSeaTouchedShell();
        } else if (roll < rareChance * 2 / 3) {
            return CustomItems.createAnglersAlmanac();
        } else if (roll < rareChance) {
            // Wind Burst enchanted book - rare at any level, scales with level
            return createEnchantedBook("wind_burst");
        }

        if (plugin.getConfigManager().isAnglersRodEnabled()
                && random.nextDouble() < plugin.getConfigManager().getAnglersRodDropChance() * qualityMultiplier) {
            return CustomItems.createAnglersRod();
        }

        return null;
    }

    /**
     * Roll for biome-specific loot.
     */
    private ItemStack rollForBiomeLoot(Biome biome, int level, double qualityMultiplier, World world) {
        double roll = random.nextDouble();
        double chance = plugin.getConfigManager().getUncommonBase() * qualityMultiplier;

        if (roll > chance)
            return null;

        String biomeName = biome.name().toLowerCase();

        // Ocean biomes
        if (biomeName.contains("ocean") || biomeName.contains("beach")) {
            return rollOceanLoot(level, biomeName.contains("deep"));
        }

        // Swamp biomes
        if (biomeName.contains("swamp") || biomeName.contains("mangrove")) {
            return rollSwampLoot(level);
        }

        // Jungle
        if (biomeName.contains("jungle")) {
            return rollJungleLoot();
        }

        // Desert
        if (biomeName.contains("desert")) {
            return rollDesertLoot(level);
        }

        // Plains/Meadow
        if (biomeName.contains("plains") || biomeName.contains("meadow")) {
            return rollPlainsLoot();
        }

        // Forest
        if (biomeName.contains("forest")) {
            return rollForestLoot(level, biomeName.contains("dark"));
        }

        // Taiga
        if (biomeName.contains("taiga") || biomeName.contains("snowy")) {
            return rollTaigaLoot();
        }

        // Badlands
        if (biomeName.contains("badlands")) {
            return rollBadlandsLoot();
        }

        // Deep Dark
        if (biomeName.contains("deep_dark")) {
            return rollDeepDarkLoot(level);
        }

        // Cherry Grove
        if (biomeName.contains("cherry")) {
            return rollCherryLoot();
        }

        // Nether (if lava fishing is unlocked at level 20+)
        if (plugin.getConfigManager().isNetherFishingEnabled()
                && world.getEnvironment() == World.Environment.NETHER
                && level >= plugin.getConfigManager().getNetherFishingLevel()) {
            return rollNetherLoot(level);
        }

        // End
        if (world.getEnvironment() == World.Environment.THE_END) {
            return rollEndLoot(level);
        }

        return null;
    }

    // Biome-specific loot methods
    private ItemStack rollOceanLoot(int level, boolean isDeep) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.PRISMARINE_CRYSTALS, random.nextInt(3) + 1));
        pool.add(new ItemStack(Material.NAUTILUS_SHELL));
        pool.add(CustomItems.createTridentShard());

        if (level >= 50 && random.nextDouble() < 0.01) {
            pool.add(new ItemStack(Material.HEART_OF_THE_SEA));
        }
        if (isDeep && level >= 80 && random.nextDouble() < 0.005) {
            pool.add(new ItemStack(Material.SPONGE));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollSwampLoot(int level) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.SLIME_BLOCK, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.SPLASH_POTION)); // Could enchant with harming

        // Triple chance for Mending in swamps
        if (random.nextDouble() < 0.03 * (1 + level * 0.02)) {
            return createEnchantedBook("mending");
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollJungleLoot() {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.BAMBOO, random.nextInt(32) + 16));
        pool.add(new ItemStack(Material.SCAFFOLDING, random.nextInt(16) + 8));
        pool.add(new ItemStack(Material.COCOA_BEANS, random.nextInt(8) + 4));
        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollDesertLoot(int level) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.GOLD_INGOT, random.nextInt(5) + 3));
        pool.add(new ItemStack(Material.TNT, random.nextInt(4) + 2));

        if (level >= 95 && random.nextDouble() < 0.001) {
            pool.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollPlainsLoot() {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.HONEY_BLOCK, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.LEAD, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.MUSIC_DISC_CAT)); // Random disc
        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollForestLoot(int level, boolean isDark) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.SADDLE));
        pool.add(new ItemStack(Material.NAME_TAG));

        if (isDark) {
            pool.add(CustomItems.createTotemShard());
            pool.add(CustomItems.createTotemShard()); // Double chance in Dark Forest
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollTaigaLoot() {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.RABBIT_FOOT));
        pool.add(new ItemStack(Material.POWDER_SNOW_BUCKET));
        pool.add(new ItemStack(Material.SWEET_BERRIES, random.nextInt(16) + 8));
        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollBadlandsLoot() {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.RAW_GOLD_BLOCK, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.GOLD_BLOCK));
        pool.add(new ItemStack(Material.TNT, random.nextInt(8) + 4));
        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollDeepDarkLoot(int level) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.ECHO_SHARD, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.SCULK_CATALYST));

        if (level >= 60 && random.nextDouble() < 0.02) {
            pool.add(createEnchantedBook("swift_sneak"));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollCherryLoot() {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.BEE_NEST));
        pool.add(new ItemStack(Material.CHERRY_LOG, random.nextInt(16) + 8));
        pool.add(new ItemStack(Material.PINK_PETALS, random.nextInt(8) + 4));
        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollNetherLoot(int level) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.GHAST_TEAR, random.nextInt(2) + 1));
        pool.add(new ItemStack(Material.MAGMA_CREAM, random.nextInt(4) + 2));

        if (level >= 80 && random.nextDouble() < 0.01) {
            pool.add(new ItemStack(Material.ANCIENT_DEBRIS));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private ItemStack rollEndLoot(int level) {
        List<ItemStack> pool = new ArrayList<>();
        pool.add(new ItemStack(Material.ENDER_PEARL, random.nextInt(4) + 2));
        pool.add(new ItemStack(Material.CHORUS_FRUIT, random.nextInt(8) + 4));

        if (level >= 90 && random.nextDouble() < 0.02) {
            pool.add(new ItemStack(Material.SHULKER_SHELL));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Roll for global treasure pool items.
     */
    private ItemStack rollForGlobalTreasure(int level, double qualityMultiplier) {
        double roll = random.nextDouble();
        double chance = plugin.getConfigManager().getCommonBase() * qualityMultiplier * 0.5; // Lower base for global

        if (roll > chance)
            return null;

        List<ItemStack> pool = new ArrayList<>();

        // Always available
        pool.add(new ItemStack(Material.NAME_TAG));
        pool.add(new ItemStack(Material.SADDLE));
        pool.add(new ItemStack(Material.NAUTILUS_SHELL));
        pool.add(CustomItems.createSunkenCrate());

        // Level-gated
        if (level >= 10) {
            pool.add(new ItemStack(Material.DIAMOND, random.nextInt(3) + 1));
            pool.add(new ItemStack(Material.LAPIS_LAZULI, random.nextInt(16) + 8));
        }

        if (level >= 20) {
            pool.add(CustomItems.createTridentShard());
            pool.add(createEnchantedBook("mending"));
        }

        if (level >= 30) {
            pool.add(new ItemStack(Material.NETHERITE_SCRAP));
            pool.add(new ItemStack(Material.SHULKER_SHELL));
        }

        if (level >= 40) {
            pool.add(CustomItems.createTotemShard());
            pool.add(new ItemStack(Material.HEART_OF_THE_SEA));
        }

        if (level >= 50) {
            pool.add(new ItemStack(Material.ANCIENT_DEBRIS));
        }

        if (level >= 60) {
            pool.add(new ItemStack(Material.GOLDEN_APPLE));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Create an enchanted book with a specific enchantment.
     */
    private ItemStack createEnchantedBook(String enchantment) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) book
                .getItemMeta();

        switch (enchantment.toLowerCase()) {
            case "mending":
                meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.MENDING, 1, true);
                break;
            case "swift_sneak":
                meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.SWIFT_SNEAK, 3, true);
                break;
            case "lure":
                meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.LURE, 3, true);
                break;
            case "luck_of_the_sea":
                meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 3, true);
                break;
            case "wind_burst":
                meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.WIND_BURST, 3, true);
                break;
        }

        book.setItemMeta(meta);
        return book;
    }
}
