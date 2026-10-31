package com.kitsunetech.fishing.systems;

import com.kitsunetech.fishing.VanillaFishingExpansion;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Tracks fish journal/collection progress per player.
 */
public class FishJournal {

    private final VanillaFishingExpansion plugin;
    private final File journalFolder;
    private final Map<UUID, JournalData> playerJournals;

    // Define collectible items per biome
    private static final Map<String, Set<Material>> BIOME_COLLECTIONS = new HashMap<>();

    static {
        // Ocean
        BIOME_COLLECTIONS.put("ocean", Set.of(
                Material.COD, Material.SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH,
                Material.NAUTILUS_SHELL, Material.PRISMARINE_CRYSTALS, Material.HEART_OF_THE_SEA));

        // Swamp
        BIOME_COLLECTIONS.put("swamp", Set.of(
                Material.COD, Material.SLIME_BALL, Material.LILY_PAD, Material.ENCHANTED_BOOK));

        // Jungle
        BIOME_COLLECTIONS.put("jungle", Set.of(
                Material.COD, Material.SALMON, Material.BAMBOO, Material.COCOA_BEANS));

        // Desert
        BIOME_COLLECTIONS.put("desert", Set.of(
                Material.COD, Material.GOLD_INGOT, Material.TNT, Material.CACTUS));

        // Forest
        BIOME_COLLECTIONS.put("forest", Set.of(
                Material.COD, Material.SALMON, Material.SADDLE, Material.NAME_TAG));

        // Taiga
        BIOME_COLLECTIONS.put("taiga", Set.of(
                Material.COD, Material.SALMON, Material.RABBIT_FOOT, Material.SWEET_BERRIES));

        // Nether
        BIOME_COLLECTIONS.put("nether", Set.of(
                Material.GHAST_TEAR, Material.MAGMA_CREAM, Material.ANCIENT_DEBRIS, Material.BLAZE_ROD));

        // End
        BIOME_COLLECTIONS.put("end", Set.of(
                Material.ENDER_PEARL, Material.CHORUS_FRUIT, Material.SHULKER_SHELL));
    }

    public FishJournal(VanillaFishingExpansion plugin) {
        this.plugin = plugin;
        this.journalFolder = new File(plugin.getDataFolder(), "journals");
        this.playerJournals = new HashMap<>();

        if (!journalFolder.exists()) {
            journalFolder.mkdirs();
        }
    }

    /**
     * Record a catch in the player's journal.
     */
    public void recordCatch(UUID playerId, Material material, String biomeGroup) {
        if (!plugin.getConfigManager().isFishJournalEnabled())
            return;

        JournalData journal = getJournal(playerId);
        journal.catches.add(material);

        if (biomeGroup != null && !biomeGroup.isEmpty()) {
            journal.biomeCatches.computeIfAbsent(biomeGroup, k -> new HashSet<>()).add(material);
        }
    }

    /**
     * Get journal data for a player.
     */
    public JournalData getJournal(UUID playerId) {
        if (!playerJournals.containsKey(playerId)) {
            JournalData data = loadJournal(playerId);
            playerJournals.put(playerId, data);
        }
        return playerJournals.get(playerId);
    }

    /**
     * Check if a biome collection is complete.
     */
    public boolean isBiomeComplete(UUID playerId, String biomeGroup) {
        JournalData journal = getJournal(playerId);
        Set<Material> required = BIOME_COLLECTIONS.get(biomeGroup);
        if (required == null)
            return false;

        Set<Material> caught = journal.biomeCatches.get(biomeGroup);
        if (caught == null)
            return false;

        return caught.containsAll(required);
    }

    /**
     * Get completion percentage for a biome.
     */
    public double getBiomeProgress(UUID playerId, String biomeGroup) {
        Set<Material> required = BIOME_COLLECTIONS.get(biomeGroup);
        if (required == null)
            return 0;

        JournalData journal = getJournal(playerId);
        Set<Material> caught = journal.biomeCatches.get(biomeGroup);
        if (caught == null)
            return 0;

        long completed = required.stream().filter(caught::contains).count();
        return (double) completed / required.size();
    }

    /**
     * Get total journal completion percentage.
     */
    public double getTotalProgress(UUID playerId) {
        int totalRequired = BIOME_COLLECTIONS.values().stream().mapToInt(Set::size).sum();
        JournalData journal = getJournal(playerId);

        Set<Material> allCaught = new HashSet<>();
        journal.biomeCatches.values().forEach(allCaught::addAll);

        Set<Material> allRequired = new HashSet<>();
        BIOME_COLLECTIONS.values().forEach(allRequired::addAll);

        long completed = allRequired.stream().filter(allCaught::contains).count();
        return (double) completed / allRequired.size();
    }

    /**
     * Get biome group name from a Biome.
     */
    public static String getBiomeGroup(Biome biome) {
        String name = biome.name().toLowerCase();
        if (name.contains("ocean") || name.contains("beach"))
            return "ocean";
        if (name.contains("swamp") || name.contains("mangrove"))
            return "swamp";
        if (name.contains("jungle"))
            return "jungle";
        if (name.contains("desert"))
            return "desert";
        if (name.contains("forest"))
            return "forest";
        if (name.contains("taiga") || name.contains("snowy"))
            return "taiga";
        if (name.contains("nether") || name.contains("crimson") || name.contains("warped"))
            return "nether";
        if (name.contains("end"))
            return "end";
        return "other";
    }

    /**
     * Get all available biome groups.
     */
    public static Set<String> getAllBiomeGroups() {
        return BIOME_COLLECTIONS.keySet();
    }

    private JournalData loadJournal(UUID playerId) {
        File file = new File(journalFolder, playerId.toString() + ".yml");
        JournalData data = new JournalData();

        if (!file.exists())
            return data;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String matName : config.getStringList("catches")) {
            try {
                data.catches.add(Material.valueOf(matName));
            } catch (IllegalArgumentException ignored) {
                // Material no longer exists (e.g. renamed between versions), skip it
            }
        }

        Set<String> biomeKeys = config.getConfigurationSection("biomes") != null
                ? config.getConfigurationSection("biomes").getKeys(false)
                : Collections.emptySet();

        for (String biome : biomeKeys) {
            Set<Material> biomeMats = new HashSet<>();
            for (String mat : config.getStringList("biomes." + biome)) {
                try {
                    biomeMats.add(Material.valueOf(mat));
                } catch (IllegalArgumentException ignored) {
                    // Material no longer exists (e.g. renamed between versions), skip it
                }
            }
            data.biomeCatches.put(biome, biomeMats);
        }

        return data;
    }

    public void saveJournal(UUID playerId) {
        JournalData data = playerJournals.get(playerId);
        if (data == null)
            return;

        File file = new File(journalFolder, playerId.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        List<String> catches = new ArrayList<>();
        data.catches.forEach(m -> catches.add(m.name()));
        config.set("catches", catches);

        for (Map.Entry<String, Set<Material>> entry : data.biomeCatches.entrySet()) {
            List<String> biomeMats = new ArrayList<>();
            entry.getValue().forEach(m -> biomeMats.add(m.name()));
            config.set("biomes." + entry.getKey(), biomeMats);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save journal for " + playerId);
        }
    }

    public void saveAll() {
        for (UUID playerId : playerJournals.keySet()) {
            saveJournal(playerId);
        }
    }

    public void unloadPlayer(UUID playerId) {
        saveJournal(playerId);
        playerJournals.remove(playerId);
    }

    /**
     * Journal data structure.
     */
    public static class JournalData {
        public Set<Material> catches = new HashSet<>();
        public Map<String, Set<Material>> biomeCatches = new HashMap<>();
    }
}
