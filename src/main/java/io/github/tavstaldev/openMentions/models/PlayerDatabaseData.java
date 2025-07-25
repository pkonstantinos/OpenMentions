package io.github.tavstaldev.openMentions.models;

import java.util.UUID;

/**
 * Represents the database data for a player.
 * This class stores information about a player's preferences and settings
 * for mention notifications.
 */
public class PlayerDatabaseData {
    /** The unique identifier of the player. */
    public UUID PlayerId;

    /** The name of the sound associated with the player's mention notifications. */
    public String SoundName;

    /** The display option for the player's mention notifications. */
    public EMentionDisplay Display;

    /** The preference for receiving mention notifications. */
    public EMentionPreference Preference;

    /**
     * Constructs a new PlayerDatabaseData instance with the specified parameters.
     *
     * @param playerId The unique identifier of the player.
     * @param soundName The name of the sound associated with the player's mention notifications.
     * @param display The display option for the player's mention notifications.
     * @param preference The preference for receiving mention notifications.
     */
    public PlayerDatabaseData(UUID playerId, String soundName, EMentionDisplay display, EMentionPreference preference) {
        PlayerId = playerId;
        SoundName = soundName;
        Display = display;
        Preference = preference;
    }
}