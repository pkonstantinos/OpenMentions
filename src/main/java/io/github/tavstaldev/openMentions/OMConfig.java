package io.github.tavstaldev.openMentions;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class OMConfig extends ConfigurationBase {

    public OMConfig() {
        super(OpenMentions.Instance, "config.yml", null);
    }

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "eng");
        resolve("usePlayerLocale", true);
        resolve("updateChecker", true);
        resolve("debug", false);
        resolve("prefix", "&3Open&bMentions &8»");

        // Dates
        resolve("dates.daily-refresh", LocalDate.now().plusDays(1).atStartOfDay().toString());
        resolve("dates.weekly-refresh", LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay().toString());

        // Storage
        resolve("storage.type", "sqlite");
        resolve("storage.filename", "database");
        resolve("storage.host", "localhost");
        resolve("storage.port", 3306);
        resolve("storage.database", "minecraft");
        resolve("storage.username", "root");
        resolve("storage.password", "ascent");
        resolve("storage.tablePrefix", "openmentions");

        // Settings
        resolve("settings.defaultDisplay", "ALL");
        resolve("settings.defaultPreference", "ALWAYS");
        resolve("settings.defaultSound", "ENTITY_PLAYER_LEVELUP");
        resolve("settings.volume", 1.0);
        resolve("settings.pitch", 1.0);
        resolve("settings.mentionCooldown", 3);
        resolve("settings.maxMentionsPerMessage", 3);
        resolve("settings.requireOnline", false);
        resolve("settings.allowSelfMention", true);

        // Formatting
        resolve("formatting.requireSymbol", false);
        resolve("formatting.symbols", new String[]{"@", "!"});
        resolve("formatting.defaultFormat", "&e@%player%&r");
        // Example permission based formats
        resolve("formatting.permissionBasedFormats", new String[]{
            "group:admin;format:&c@%player%&r",
            "group:vip;format:&d@%player%&r"
        });
    }
}
