package com.kitsunetech.fishing;

import com.kitsunetech.fishing.commands.FishingStatsCommand;
import com.kitsunetech.fishing.commands.FishJournalCommand;
import com.kitsunetech.fishing.commands.FishTopCommand;
import com.kitsunetech.fishing.config.ConfigManager;
import com.kitsunetech.fishing.data.FisherDataManager;
import com.kitsunetech.fishing.listeners.FishingListener;
import com.kitsunetech.fishing.listeners.ItemInteractionListener;
import com.kitsunetech.fishing.listeners.PassiveEffectListener;
import com.kitsunetech.fishing.loot.LootManager;
import com.kitsunetech.fishing.systems.EventManager;
import com.kitsunetech.fishing.systems.FishJournal;
import com.kitsunetech.fishing.systems.LeaderboardManager;
import com.kitsunetech.fishing.systems.StreakManager;
import com.kitsunetech.fishing.items.CustomItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaFishingExpansion extends JavaPlugin {

    private static VanillaFishingExpansion instance;

    // Core managers
    private ConfigManager configManager;
    private FisherDataManager fisherDataManager;
    private LootManager lootManager;
    private PassiveEffectListener passiveEffectListener;

    // Phase 2 systems
    private StreakManager streakManager;
    private LeaderboardManager leaderboardManager;
    private EventManager eventManager;
    private FishJournal fishJournal;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config first
        configManager = new ConfigManager(this);

        // Initialize core managers
        fisherDataManager = new FisherDataManager(this);
        lootManager = new LootManager(this);

        // Initialize Phase 2 systems
        streakManager = new StreakManager(this);
        leaderboardManager = new LeaderboardManager(this);
        eventManager = new EventManager(this);
        fishJournal = new FishJournal(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemInteractionListener(this), this);
        passiveEffectListener = new PassiveEffectListener(this);
        getServer().getPluginManager().registerEvents(passiveEffectListener, this);

        // Register commands
        getCommand("fishingstats").setExecutor(new FishingStatsCommand(this));
        getCommand("fishtop").setExecutor(new FishTopCommand(this));
        getCommand("fishjournal").setExecutor(new FishJournalCommand(this));

        // Register shard crafting recipes
        registerShardRecipes();

        getLogger().info("VanillaFishingExpansion enabled! Silent leveling active.");
        getLogger().info("Phase 2 Features: Streak System, Leaderboard, Fish Journal, Seasonal Events");
    }

    @Override
    public void onDisable() {
        if (fisherDataManager != null) {
            fisherDataManager.saveAllData();
        }
        if (leaderboardManager != null) {
            leaderboardManager.save();
        }
        if (fishJournal != null) {
            fishJournal.saveAll();
        }
        getLogger().info("VanillaFishingExpansion disabled. All data saved.");
    }

    /**
     * Register crafting recipes for shard items.
     */
    private void registerShardRecipes() {
        getServer().addRecipe(buildTridentRecipe());
        getServer().addRecipe(buildTotemRecipe());
        getLogger().info("Registered shard crafting recipes.");
    }

    /**
     * Build the "9 Trident Shards -> Trident" recipe. The ingredient is matched by
     * ExactChoice against the plugin's PDC-tagged Trident Shard, so plain vanilla
     * prismarine shards can NOT satisfy it.
     */
    public ShapedRecipe buildTridentRecipe() {
        ShapedRecipe tridentRecipe = new ShapedRecipe(
                new NamespacedKey(this, "trident_from_shards"),
                new ItemStack(Material.TRIDENT));
        tridentRecipe.shape("SSS", "SSS", "SSS");
        tridentRecipe.setIngredient('S', new RecipeChoice.ExactChoice(CustomItems.createTridentShard()));
        return tridentRecipe;
    }

    /**
     * Build the "4 Totem Shards -> Totem of Undying" recipe. The ingredient is matched
     * by ExactChoice against the plugin's PDC-tagged Totem Shard, so plain vanilla gold
     * nuggets can NOT satisfy it.
     */
    public ShapedRecipe buildTotemRecipe() {
        ShapedRecipe totemRecipe = new ShapedRecipe(
                new NamespacedKey(this, "totem_from_shards"),
                new ItemStack(Material.TOTEM_OF_UNDYING));
        totemRecipe.shape(" S ", "SS ", " S ");
        totemRecipe.setIngredient('S', new RecipeChoice.ExactChoice(CustomItems.createTotemShard()));
        return totemRecipe;
    }

    public static VanillaFishingExpansion getInstance() {
        return instance;
    }

    // Core getters
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FisherDataManager getFisherDataManager() {
        return fisherDataManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public PassiveEffectListener getPassiveEffectListener() {
        return passiveEffectListener;
    }

    // Phase 2 getters
    public StreakManager getStreakManager() {
        return streakManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public FishJournal getFishJournal() {
        return fishJournal;
    }
}
