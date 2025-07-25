package io.github.tavstaldev.openMentions.models;

import org.bukkit.entity.Player;

/**
 * Interface for managing combat-related features in the OpenMentions plugin.
 * Provides methods to check if a player is currently in combat.
 */
public interface ICombatManager {
    /**
     * Checks if the specified player is currently in combat.
     *
     * @param player The player to check.
     * @return True if the player is in combat, false otherwise.
     */
    boolean isPlayerInCombat(Player player);
}