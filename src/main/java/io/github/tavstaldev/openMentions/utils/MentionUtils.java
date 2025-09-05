package io.github.tavstaldev.openMentions.utils;

import com.cryptomorin.xseries.XSound;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.managers.PlayerCacheManager;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for handling player mentions in the OpenMentions plugin.
 * Provides methods for formatting mentions and notifying players.
 */
public class MentionUtils {
    /** Logger instance for logging messages related to MentionUtils. */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(MentionUtils.class);

    /**
     * Retrieves the formatted mention string for a player.
     * The format is determined by the player's permissions or the default format in the configuration.
     *
     * @param player The player for whom the mention format is retrieved.
     * @return The formatted mention string.
     */
    public static String getFormattedMention(Player player) {
        var config = OpenMentions.GetConfig();
        String format = config.getString("formatting.defaultFormat");
        var permissionFormats = config.get("formatting.permissionBasedFormats");
        if (permissionFormats != null) {
            List<Map<String, String>> rawPermissionFormats = TypeUtils.castAsListOfMaps(permissionFormats, null);
            if (rawPermissionFormats != null) {
                for (var entry : rawPermissionFormats) {
                    String permissionKey = entry.get("group");
                    String permissionFormat = entry.get("format");
                    if (player.hasPermission(String.format("openmentions.format.%s", permissionKey))) {
                        format = permissionFormat;
                        break; // Use the first matching permission format
                    } else {
                        _logger.Debug("Player " + player.getName() + " does not have permission for format: " + permissionKey);
                    }
                }
            }
            else
                _logger.Warn("Permission formats in the config are not in the expected format. Expected a list of maps.");
        }
        else
            _logger.Warn("No permission formats found in the config. Using default format.");

        if (format == null || format.isEmpty()) {
            _logger.Error("Default format is not set in the config. Please set 'formatting.defaultFormat' in the config.yml.");
            return player.getName(); // Fallback to player name if format is not set
        }
        return format.replace("%player%", player.getName());
    }

    /**
     * Handles the mention of a player by another player.
     * Determines the player's mention preferences and sends the appropriate notification.
     *
     * @param player The player being mentioned.
     * @param mentioner The player who mentioned the target player.
     */
    public static void mentionPlayer(@NotNull Player player, Player mentioner) {
        var playerId = player.getUniqueId();
        var data = PlayerCacheManager.getPlayerData(playerId);
        if (data == null) {
            _logger.Error("Player cache not found for " + player.getName());
            return;
        }

        var mentionerId = mentioner.getUniqueId();
        if (PlayerCacheManager.isOnCooldown(mentionerId))
            return; // Do not notify

        switch (data.Preference)
        {
            case ALWAYS: {
                sendMention(player, data.SoundName, data.Display, false, mentioner);
                break;
            }
            case SILENT_IN_COMBAT: {
                sendMention(player, data.SoundName, data.Display, OpenMentions.CombatManager.isPlayerInCombat(player), mentioner);
                break;
            }
            case NEVER_IN_COMBAT: {
                if (OpenMentions.CombatManager.isPlayerInCombat(player))
                    break; // Player is in combat, do not mention
                sendMention(player, data.SoundName, data.Display, false, mentioner);
                break;
            }
            case NEVER: {
                // Do nothing, player has disabled mentions
                break;
            }
        }

        var cooldownTime = OpenMentions.GetConfig().getInt("settings.mentionCooldown");
        if (cooldownTime < 1)
            return;

        PlayerCacheManager.setCooldown(mentionerId, LocalDateTime.now().plusSeconds(cooldownTime));
    }

    /**
     * Sends a mention notification to a player.
     * The notification can include chat messages, action bar messages, and sounds based on the player's preferences.
     *
     * @param player The player to notify.
     * @param soundKey The key of the sound to play.
     * @param display The display type for the mention notification.
     * @param isSilent Whether the notification should be silent (no sound).
     * @param mentioner The player who mentioned the target player.
     */
    private static void sendMention(Player player, String soundKey, EMentionDisplay display, boolean isSilent, Player mentioner) {
        String actionBarMessage = OpenMentions.Instance.getTranslator().Localize("General.ActionBarMessage", Map.of("player", mentioner.getName()));
        float volume = (float)OpenMentions.GetConfig().getDouble("settings.volume");
        float pitch = (float)OpenMentions.GetConfig().getDouble("settings.pitch");
        XSound sound;
        Optional<XSound> soundResult = SoundUtils.getSound(soundKey);
        // Fallback sound if not found
        sound = soundResult.orElse(XSound.ENTITY_PLAYER_LEVELUP);

        switch (display) {
            case ALL: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case ONLY_CHAT: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                break;
            }
            case ONLY_SOUND: {
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case ONLY_ACTIONBAR: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case CHAT_AND_SOUND: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case CHAT_AND_ACTIONBAR: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case ACTIONBAR_AND_SOUND: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
        }
    }
}