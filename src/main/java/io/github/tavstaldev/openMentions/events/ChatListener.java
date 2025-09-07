package io.github.tavstaldev.openMentions.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.utils.MentionUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(ChatListener.class);

    public ChatListener() {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenMentions.Instance);
        _logger.Debug("Event listener registered.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player source = event.getPlayer();
        String rawMessage = event.getMessage();

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
        //boolean requireOnline = OpenMentions.GetConfig().getBoolean("settings.requireOnline");

        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Ignore the sender player to prevent self-mentions
            if (!allowSelfMention && onlinePlayer.getUniqueId() == source.getUniqueId())
                continue;

            String onlinePlayerName = onlinePlayer.getName();
            if (!rawMessage.contains(onlinePlayerName))
                continue;

            String mentionPrefix = MentionUtils.getFormattedMention(onlinePlayer);
            String localRegexPattern = regexPattern.toString().replace("{player}", onlinePlayerName);
            rawMessage = rawMessage.replaceAll(localRegexPattern, mentionPrefix);
            MentionUtils.mentionPlayer(onlinePlayer, source);
            mentionCount++;
            if (mentionCount >= maxMentionCount) {
                _logger.Debug(String.format("Player %s has exceeded the maximum mention count (%d) in a single message.", source.getName(), maxMentionCount));
                break;
            }
        }
        event.setMessage(rawMessage.replace("&", "§"));
    }
}
