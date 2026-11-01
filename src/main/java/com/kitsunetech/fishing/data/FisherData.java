package com.kitsunetech.fishing.data;

import java.util.UUID;

/**
 * Represents a player's fishing data including level, XP, and permanent buffs.
 */
public class FisherData {

    private final UUID playerId;
    private int level;
    private int totalFishCaught;
    private int fishForCurrentLevel;

    // Permanent buffs from consuming items
    private double permanentSpeedBonus; // From Neptune's Ledger / Angler's Almanac
    private double permanentLuckBonus; // From Neptune's Ledger / Angler's Almanac
    private boolean hasConsumedLedger; // Elder Fisherman particles unlock

    private final int baseFishRequired;
    private final double scalingMultiplier;
    private final int maxLevel;

    public FisherData(UUID playerId) {
        this(playerId, 1, 0, 0, 0.0, 0.0, false, 10, 1.025, 100);
    }

    public FisherData(UUID playerId, int baseFishRequired, double scalingMultiplier, int maxLevel) {
        this(playerId, 1, 0, 0, 0.0, 0.0, false, baseFishRequired, scalingMultiplier, maxLevel);
    }

    public FisherData(UUID playerId, int level, int totalFishCaught, int fishForCurrentLevel,
            double permanentSpeedBonus, double permanentLuckBonus, boolean hasConsumedLedger,
            int baseFishRequired, double scalingMultiplier, int maxLevel) {
        this.playerId = playerId;
        this.level = level;
        this.totalFishCaught = totalFishCaught;
        this.fishForCurrentLevel = fishForCurrentLevel;
        this.permanentSpeedBonus = permanentSpeedBonus;
        this.permanentLuckBonus = permanentLuckBonus;
        this.hasConsumedLedger = hasConsumedLedger;
        this.baseFishRequired = Math.max(1, baseFishRequired);
        this.scalingMultiplier = Math.max(0.0, scalingMultiplier);
        this.maxLevel = Math.max(1, maxLevel);
    }

    public FisherData(UUID playerId, int level, int totalFishCaught, int fishForCurrentLevel,
            double permanentSpeedBonus, double permanentLuckBonus, boolean hasConsumedLedger) {
        this(playerId, level, totalFishCaught, fishForCurrentLevel, permanentSpeedBonus, permanentLuckBonus,
                hasConsumedLedger, 10, 1.025, 100);
    }

    /**
     * Calculate fish needed for a specific level.
     * Level 1 = 10 fish, then +2.5% compound per level.
     */
    public static int getFishRequiredForLevel(int level) {
        if (level <= 1)
            return 10;
        return (int) Math.ceil(10 * Math.pow(1.025, level - 1));
    }

    public int getFishRequiredForLevel() {
        if (level <= 1)
            return baseFishRequired;
        return (int) Math.ceil(baseFishRequired * Math.pow(scalingMultiplier, level - 1));
    }

    /**
     * Get total cumulative fish needed to reach a level.
     */
    public static int getCumulativeFishForLevel(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += getFishRequiredForLevel(i);
        }
        return total;
    }

    /**
     * Add a caught fish and check for level up. Returns true if leveled up.
     */
    public boolean addFish() {
        totalFishCaught++;
        fishForCurrentLevel++;

        int required = getFishRequiredForLevel();
        if (fishForCurrentLevel >= required && level < maxLevel) {
            level++;
            fishForCurrentLevel = 0;
            return true; // Silent level up
        }
        return false;
    }

    /**
     * Get the quality bonus multiplier (2.5% compound per level).
     * Level 1 = 1.0, Level 100 = ~11.8x
     */
    public double getQualityMultiplier() {
        return Math.pow(scalingMultiplier, level - 1);
    }

    /**
     * Get the speed bonus (reduces fishing wait time).
     * Combines level bonus + permanent buffs.
     */
    public double getTotalSpeedBonus() {
        double levelBonus = 1.0 - Math.pow(0.975, level - 1); // 2.5% reduction per level
        return Math.min(0.75, levelBonus + permanentSpeedBonus); // Cap at 75% reduction
    }

    /**
     * Get durability save chance (0.5% per level).
     */
    public double getDurabilitySaveChance() {
        return Math.min(0.5, level * 0.005); // Cap at 50%
    }

    /**
     * Get total luck bonus (level + permanent).
     */
    public double getTotalLuckBonus() {
        return getQualityMultiplier() + permanentLuckBonus;
    }

    public void addPermanentSpeedBonus(double bonus) {
        this.permanentSpeedBonus += bonus;
    }

    public void addPermanentLuckBonus(double bonus) {
        this.permanentLuckBonus += bonus;
    }

    // Getters
    public UUID getPlayerId() {
        return playerId;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalFishCaught() {
        return totalFishCaught;
    }

    public int getFishForCurrentLevel() {
        return fishForCurrentLevel;
    }

    public double getPermanentSpeedBonus() {
        return permanentSpeedBonus;
    }

    public double getPermanentLuckBonus() {
        return permanentLuckBonus;
    }

    public boolean hasConsumedLedger() {
        return hasConsumedLedger;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setConsumedLedger(boolean consumed) {
        this.hasConsumedLedger = consumed;
    }
}
