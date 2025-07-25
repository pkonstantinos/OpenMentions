package io.github.tavstaldev.openMentions.utils;

import com.cryptomorin.xseries.XSound;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for handling sound-related operations.
 * Provides methods to retrieve sound objects based on their names.
 */
public class SoundUtils {
    /** Logger instance for logging messages related to SoundUtils. */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(SoundUtils.class);

    /**
     * Retrieves an XSound object based on the provided sound name.
     *
     * @param name The name of the sound to retrieve. Must not be null.
     * @return An Optional containing the XSound object if found, or an empty Optional if the name is "none" or invalid.
     */
    public static Optional<XSound> getSound(@NotNull String name) {
        try {
            String key = name.toLowerCase(Locale.ROOT);
            // Fixes null pointer exception
            if ("none".equalsIgnoreCase(key))
                return Optional.empty();

            return XSound.of(key);
        }
        catch (Exception ex) {
            _logger.Debug("Failed to get sound for name: " + name);
            _logger.Debug("Exception: " + ex.getMessage());
            return Optional.empty();
        }
    }
}