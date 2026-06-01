package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public enum Locales {

    COMMAND_RELOAD_DESCRIPTION("Commands.Reload.Description", "reloads the plugin."),
    COMMAND_CLEAR_DESCRIPTION("Commands.Clear.Description", "clears the chat."),
    COMMAND_CLEAR_CONSOLE("Commands.Clear.Console", "CONSOLE"),
    COMMAND_CLEAR_UNKNOWN("Commands.Clear.Unknown", "UNKNOWN"),
    MESSAGES_RELOAD("Messages.Commands.Reload.Success", "&a✔ &fConfiguration and locales successfully reloaded!"),
    MESSAGES_CLEAR("Messages.Commands.Clear.Success", "&4⚠ &fThe chat was cleared by &4%clearer%&f."),
    MESSAGES_AD("Messages.Chat.AdDetected", "&c✘ &fAdvertising is not allowed!"),
    MESSAGES_BLOCKED("Messages.Chat.BlockedWord", "&c✘ &fThis word is not allowed!"),
    MESSAGES_AD_NOTIFY("Messages.Chat.AdNotify", "&4⚠ %player &ftried to write an ad in chat!"),
    NO_COLOR_PERMISSION("Messages.NoColorPermission", "&c✘ &fYou don't have permission to use legacy colors!"),
    NO_HEX_PERMISSION("Messages.NoHexPermission", "&c✘ &fYou don't have permission to use HEX colors!"),
    MAGIC_BLOCKED("Messages.MagicBlocked", "&c✘ &fYou don't have permission to use magic color!"),
    COMMAND_RESULT_NO_PERM("Messages.CommandResult.NoPermission", "&c✘ &fYou don't have permission for this! Permission: &c%perm&f."),
    COMMAND_RESULT_WRONG_USAGE("Messages.CommandResult.WrongUsage", "&c✘ &fWrong usage! Please type &c%cmd help&f!"),
    ANTI_SPAM_DENIED("Messages.AntiSpam.Denied", "&c✘ &fSpam is not allowed! Please wait another &c%time% &fseconds!"),
    PLAYER_JOIN("Messages.Player.Join", "&f● &e%prefix%displayname%suffix &fjoined the game!"),
    PLAYER_JOIN_FIRST_TIME("Messages.Player.JoinFirstTime", "&f● &e%prefix%displayname%suffix &fjoined the server for the first time!"),
    PLAYER_KICK("Messages.Player.Kick", "&f● &e%prefix%displayname%suffix &fwas kicked from the game!"),
    PLAYER_QUIT("Messages.Player.Quit", "&f● &e%prefix%displayname%suffix &fleft the game!"),
    NO_LISTENING_PLAYERS("Messages.Chat.NoOneListens", "&4⚠ &fNo players are near you to hear you talking! Try to use the global mode to chat globally!");

    private static final File localeFolder = new File(ChatEx.getInstance().getDataFolder(), "locales");
    private static YamlConfiguration cfg;
    private static File f;
    private final String value;
    private final String path;

    Locales(String path, String val) {
        this.path = path;
        this.value = val;
    }

    public static void load() {
        localeFolder.mkdirs();
        f = new File(localeFolder, Config.LOCALE.getString() + ".yml");
        if (!f.exists()) {
            try {
                ChatEx.getInstance().saveResource("locales" + File.separator + Config.LOCALE.getString() + ".yml", true);
                File locale = new File(ChatEx.getInstance().getDataFolder(), Config.LOCALE.getString() + ".yml");
                if (locale.exists()) {
                    locale.delete();
                }
                reload(false);
            } catch (IllegalArgumentException ex) {
                reload(false);
                saveMissingDefaults();
            }
        } else {
            reload(false);
            saveMissingDefaults();
        }
    }
    
    private static void saveMissingDefaults() {
        try {
            boolean changed = false;
            for (Locales c : values()) {
                if (!cfg.contains(c.getPath())) {
                    c.set(c.getDefaultValue(), false);
                    changed = true;
                }
            }
            if (changed) {
                cfg.save(f);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public static void reload(boolean complete) {
        if (complete) {
            load();
        } else {
            cfg = YamlConfiguration.loadConfiguration(f);
        }
    }
    
    public static void fullReload() {
        load();
        ChatEx.getInstance().getLogger().info("Locales have been reloaded!");
    }

    public String getPath() {
        return path;
    }

    public String getDefaultValue() {
        return value;
    }

    public String getStringRaw() {
        return cfg != null ? cfg.getString(path, value) : value;
    }

    public String getString(Player p) {
        String raw = getStringRaw();
        String ret = Utils.replaceColors(raw);
        ret = Utils.replacePlayerPlaceholders(p, ret);
        return ret;
    }

    public String getString(Player p, Map<String, String> additionalPlaceholders) {
        String raw = getStringRaw();
        for (Map.Entry<String, String> entry : additionalPlaceholders.entrySet()) {
            raw = raw.replace(entry.getKey(), entry.getValue());
        }
        String ret = Utils.replaceColors(raw);
        ret = Utils.replacePlayerPlaceholders(p, ret);
        return ret;
    }

    public Component getComponent(Player p) {
        String raw = getStringRaw();
        raw = Utils.replacePlayerPlaceholders(p, raw, false);
        return Utils.toComponent(raw);
    }

    public Component getComponent(Player p, Map<String, String> additionalPlaceholders) {
        String raw = getStringRaw();
        for (Map.Entry<String, String> entry : additionalPlaceholders.entrySet()) {
            raw = raw.replace(entry.getKey(), entry.getValue());
        }
        raw = Utils.replacePlayerPlaceholders(p, raw, false);
        return Utils.toComponent(raw);
    }

    public void set(Object value, boolean save) {
        cfg.set(path, value);
        if (save) {
            try {
                cfg.save(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            reload(false);
        }
    }
}