package io.github.tavstaldev.openMentions.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.managers.PlayerCacheManager;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import io.github.tavstaldev.openMentions.models.PlayerDatabaseData;
import io.github.tavstaldev.openMentions.utils.MentionUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener class for handling player-related events in the OpenMentions plugin.
 * Includes player join, quit, and chat events.
 */
public class PlayerListener implements Listener {
    /** Logger instance for logging messages related to EventListener. */
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(PlayerListener.class);

    /**
     * Initializes and registers the event listener with the Bukkit plugin manager.
     */
    public PlayerListener() {
        _logger.Debug("Registering player event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenMentions.Instance);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the PlayerJoinEvent.
     * Loads or creates player data and adds it to the PlayerCacheManager.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var config = OpenMentions.GetConfig();
        Player player = event.getPlayer();
        var playerId = player.getUniqueId();
        PlayerDatabaseData databaseData = OpenMentions.Database.getData(playerId);
        if (databaseData == null) {
            var defaultSoundKey = config.getString("settings.defaultSound");
            var defaultDisplay = EMentionDisplay.valueOf(config.getString("settings.defaultDisplay"));
            var defaultPreference = EMentionPreference.valueOf(config.getString("settings.defaultPreference"));
            databaseData = new PlayerDatabaseData(
                    playerId,
                    defaultSoundKey,
                    defaultDisplay,
                    defaultPreference
            );
            OpenMentions.Database.addData(playerId, defaultSoundKey, defaultDisplay, defaultPreference);
        }

        PlayerCacheManager.addPlayerData(playerId, databaseData);
    }

    /**
     * Handles the PlayerQuitEvent.
     * Removes the player's data from the PlayerCacheManager.
     *
     * @param event The PlayerQuitEvent triggered when a player leaves the server.
     */
    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerCacheManager.removePlayerData(player.getUniqueId());
    }
}