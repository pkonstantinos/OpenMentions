package io.github.tavstaldev.openMentions.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.utils.MentionUtils;
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

        boolean requirePrefix = OpenMentions.Config().requireSymbol;
        int mentionCount = 0;
        final int maxMentionCount = OpenMentions.Config().maxMentionsPerMessage;

        // Build regex pattern for mention prefixes
        StringBuilder regexPattern = new StringBuilder();
        boolean isFirst = true;
        for (var prefix : OpenMentions.Config().symbols) {
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

        boolean allowSelfMention = OpenMentions.Config().allowSelfMention;

        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Ignore the sender player to prevent self-mentions
            if (!allowSelfMention && onlinePlayer.getUniqueId() == source.getUniqueId())
                continue;

            String onlinePlayerName = onlinePlayer.getName();
            if (!rawMessage.contains(onlinePlayerName))
                continue;

            if(OpenMentions.EssentialsX.getUser(onlinePlayer).isVanished()){
                continue;
            }

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
