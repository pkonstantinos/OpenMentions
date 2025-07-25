package io.github.tavstaldev.openMentions.managers;

import io.github.tavstaldev.openMentions.models.PlayerDatabaseData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the caching of player data.
 * This class provides methods to add, remove, and retrieve player data
 * stored in a cache for efficient access.
 */
public class PlayerCacheManager {
    /** A map storing player data, keyed by the player's unique identifier (UUID). */
    private static final Map<UUID, PlayerDatabaseData> _playerData = new HashMap<>();
    private static final Map<UUID, LocalDateTime> _cooldown = new HashMap<>();

    /**
     * Adds or updates the cached data for a player.
     *
     * @param playerId The unique identifier of the player.
     * @param playerData The data to be cached for the player.
     */
    public static void addPlayerData(UUID playerId, PlayerDatabaseData playerData) {
        _playerData.put(playerId, playerData);
    }

    /**
     * Removes the cached data for a player.
     *
     * @param playerId The unique identifier of the player whose data should be removed.
     */
    public static void removePlayerData(UUID playerId) {
        _playerData.remove(playerId);
    }

    /**
     * Retrieves the cached data for a player.
     *
     * @param playerId The unique identifier of the player.
     * @return The cached data for the player, or null if no data is found.
     */
    public static PlayerDatabaseData getPlayerData(UUID playerId) {
        return _playerData.get(playerId);
    }

    /**
     * Sets a cooldown time for a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @param time The time until which the cooldown is active.
     */
    public static void setCooldown(UUID playerId, LocalDateTime time) {
        _cooldown.put(playerId, time);
    }

    /**
     * Checks if a specific player is currently on cooldown.
     *
     * @param playerId The unique identifier of the player.
     * @return True if the player is on cooldown, false otherwise.
     */
    public static boolean isOnCooldown(UUID playerId) {
        LocalDateTime cooldownTime = _cooldown.get(playerId);
        if (cooldownTime == null) {
            return false; // No cooldown set for this player
        }
        return LocalDateTime.now().isBefore(cooldownTime); // Check if current time is before the cooldown time
    }
}