package io.github.tavstaldev.openMentions.managers;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import io.github.tavstaldev.openMentions.models.IDatabase;
import io.github.tavstaldev.openMentions.models.PlayerDatabaseData;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages SQLite database operations for the OpenMentions plugin.
 * Implements the IDatabase interface to handle player data storage and retrieval.
 */
public class SqlLiteManager implements IDatabase {
    /**
     * Retrieves the plugin configuration.
     *
     * @return The plugin's configuration file.
     */
    private static FileConfiguration getConfig() { return OpenMentions.Instance.getConfig(); }

    /** Logger instance for logging messages related to SqlLiteManager. */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(SqlLiteManager.class);

    /**
     * Loads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void load() {}

    /**
     * Unloads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void unload() {}

    /**
     * Creates a connection to the SQLite database.
     *
     * @return A Connection object to the SQLite database, or null if an error occurs.
     */
    public Connection CreateConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenMentions/%s.db", getConfig().getString("storage.filename")));
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the database schema is created. Creates the players table if it does not exist.
     */
    @Override
    public void checkSchema() {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_players (" +
                            "PlayerId VARCHAR(36) PRIMARY KEY, " +
                            "Sound VARCHAR(200) NOT NULL, " +
                            "Display VARCHAR(32) NOT NULL, " +
                            "Preference VARCHAR(32) NOT NULL);",
                    getConfig().getString("storage.tablePrefix")
            );
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Adds a new player's data to the database.
     *
     * @param playerId   The UUID of the player.
     * @param soundKey   The sound key associated with the player.
     * @param display    The display preference of the player.
     * @param preference The mention preference of the player.
     */
    @Override
    public void addData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("INSERT INTO %s_players (PlayerId, Sound, Display, Preference) " +
                            "VALUES (?, ?, ?, ?);",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, soundKey);
                statement.setString(3, display.name());
                statement.setString(4, preference.name());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the sound key for a specific player in the database.
     *
     * @param playerId The UUID of the player.
     * @param soundKey The new sound key to associate with the player.
     */
    @Override
    public void updateSound(UUID playerId, String soundKey) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Sound=? WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, soundKey);
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the display preference for a specific player in the database.
     *
     * @param playerId The UUID of the player.
     * @param display  The new display preference to associate with the player.
     */
    @Override
    public void updateDisplay(UUID playerId, EMentionDisplay display) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Display=? WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, display.name());
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the mention preference for a specific player in the database.
     *
     * @param playerId   The UUID of the player.
     * @param preference The new mention preference to associate with the player.
     */
    @Override
    public void updatePreference(UUID playerId, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Preference=? WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, preference.name());
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates all data for a specific player in the database.
     *
     * @param playerId   The UUID of the player.
     * @param soundKey   The new sound key to associate with the player.
     * @param display    The new display preference to associate with the player.
     * @param preference The new mention preference to associate with the player.
     */
    @Override
    public void updateData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Sound=?, Display=?, Preference=? WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, soundKey);
                statement.setString(2, display.name());
                statement.setString(3, preference.name());
                statement.setString(4, playerId.toString());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a player's data from the database.
     *
     * @param playerId The UUID of the player to remove.
     */
    @Override
    public void removeData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("DELETE FROM %s_players WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Checks if a player's data exists in the database.
     *
     * @param playerId The UUID of the player to check.
     * @return True if the player's data exists, false otherwise.
     */
    @Override
    public boolean hasData(UUID playerId) {
        return getData(playerId) != null;
    }

    /**
     * Retrieves all player data from the database.
     *
     * @return A list of PlayerDatabaseData objects representing all players' data.
     */
    @Override
    public List<PlayerDatabaseData> getDatas() {
        List<PlayerDatabaseData> data = new ArrayList<>();
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_players;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new PlayerDatabaseData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getString("Sound"),
                                EMentionDisplay.valueOf(result.getString("Display")),
                                EMentionPreference.valueOf(result.getString("Preference"))
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while getting player data list...\n%s", ex.getMessage()));
            return null;
        }
        return data;
    }

    /**
     * Retrieves a specific player's data from the database.
     *
     * @param playerId The UUID of the player to retrieve.
     * @return A PlayerDatabaseData object representing the player's data, or null if not found.
     */
    @Override
    public @Nullable PlayerDatabaseData getData(UUID playerId) {
        PlayerDatabaseData data = null;
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_players WHERE PlayerId=? LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new PlayerDatabaseData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getString("Sound"),
                                EMentionDisplay.valueOf(result.getString("Display")),
                                EMentionPreference.valueOf(result.getString("Preference"))
                        );
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while finding player data...\n%s", ex.getMessage()));
            return null;
        }
        return data;
    }
}