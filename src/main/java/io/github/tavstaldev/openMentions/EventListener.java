package io.github.tavstaldev.openMentions;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener class for handling player-related events in the OpenMentions plugin.
 * Includes player join, quit, and chat events.
 */
public class EventListener implements Listener {
    /** Logger instance for logging messages related to EventListener. */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(EventListener.class);

    /**
     * Initializes and registers the event listener with the Bukkit plugin manager.
     */
    public static void init() {
        _logger.Debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenMentions.Instance);
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

    /**
     * Handles the AsyncChatEvent.
     * Processes mentions in chat messages and notifies mentioned players.
     *
     * @param event The AsyncChatEvent triggered when a player sends a chat message.
     */
    @EventHandler
    public void onPlayerChatted(AsyncChatEvent event) {
        Player senderPlayer = event.getPlayer();
        //noinspection ConstantValue
        if (senderPlayer == null || !senderPlayer.isOnline()) {
            return; // Player is not online, ignore the event
        }

        Component messageComponent = event.message();
        String rawMessage = LegacyComponentSerializer.legacyAmpersand().serialize(messageComponent);

        boolean requirePrefix = OpenMentions.GetConfig().getBoolean("formatting.requirePrefix");
        int mentionCount = 0;
        final int maxMentionCount = OpenMentions.GetConfig().getInt("settings.maxMentionsPerMessage");

        // Build regex pattern for mention prefixes
        StringBuilder regexPattern = new StringBuilder();
        boolean isFirst = true;
        for (var prefix : OpenMentions.GetConfig().getStringList("formatting.symbols")) {
            if (!isFirst) {
                regexPattern.append(String.format("|%s{player}", prefix));
            } else {
                isFirst = false;
                regexPattern.append(String.format("%s{player}", prefix));
            }
        }
        if (!requirePrefix) {
            regexPattern.append("|{player}");
        }

        boolean allowSelfMention = OpenMentions.GetConfig().getBoolean("settings.allowSelfMention");
        boolean requireOnline = OpenMentions.GetConfig().getBoolean("settings.requireOnline");

        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Ignore the sender player to prevent self-mentions
            if (!allowSelfMention && onlinePlayer.getUniqueId() == senderPlayer.getUniqueId())
                continue;

            String onlinePlayerName = onlinePlayer.getName();
            if (!rawMessage.contains(onlinePlayerName))
                continue;

            String mentionPrefix = MentionUtils.getFormattedMention(onlinePlayer);
            String localRegexPattern = regexPattern.toString().replace("{player}", onlinePlayerName);
            rawMessage = rawMessage.replaceAll(localRegexPattern, mentionPrefix);
            MentionUtils.mentionPlayer(onlinePlayer, senderPlayer);
            mentionCount++;
            if (mentionCount >= maxMentionCount) {
                _logger.Debug(String.format("Player %s has exceeded the maximum mention count (%d) in a single message.", senderPlayer.getName(), maxMentionCount));
                break;
            }
        }

        event.message(LegacyComponentSerializer.legacyAmpersand().deserialize(rawMessage));
    }
}