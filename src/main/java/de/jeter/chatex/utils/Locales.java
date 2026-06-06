package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public enum Locales {

    HELP_MESSAGE("Messages.Help.Message", "&f\n&e☀ &fHelp for chat:\n&f● &e/%cmd% clear &f— clears the chat.\n&f● &e/%cmd% help &f— help for chat.\n&f● &e/%cmd% reload &f— reloads the plugin.\n&f"),
    COMMAND_CLEAR_CONSOLE("Commands.Clear.Console", "CONSOLE"),
    COMMAND_CLEAR_UNKNOWN("Commands.Clear.Unknown", "UNKNOWN"),
    MESSAGES_RELOAD("Messages.Commands.Reload.Success", "&a✔ &fConfiguration and locales successfully reloaded!"),
    MESSAGES_CLEAR("Messages.Commands.Clear.Success", "&4⚠ &fThe chat was cleared by &4%clearer%&f."),
    MESSAGES_AD("Messages.Chat.AdDetected", "&c✘ &fAdvertising is not allowed!"),
    MESSAGES_BLOCKED("Messages.Chat.BlockedWord", "&c✘ &fThis word is not allowed!"),
    MESSAGES_AD_NOTIFY("Messages.Chat.AdNotify", "&4⚠ %player% &ftried to write &4%message% &fin chat!"),
    NO_COLOR_PERMISSION("Messages.NoColorPermission", "&c✘ &fYou don't have permission to use legacy colors!"),
    NO_MODIFIER_PERMISSION("Messages.NoModifierPermission", "&c✘ &fYou don't have permission to use formatting codes!"),
    NO_HEX_PERMISSION("Messages.NoHexPermission", "&c✘ &fYou don't have permission to use HEX colors!"),
    MAGIC_BLOCKED("Messages.MagicBlocked", "&c✘ &fYou don't have permission to use magic color!"),
    COLOR_USAGE("Messages.Color.Usage", "&e☀ &fUsage: &e/color <&e&&e#HEXHEX | #HEXHEX | &e&&ex | off>"),
    COLOR_SET_SUCCESS("Messages.Color.Set", "&a✔ &fYour chat color has been updated!"),
    COLOR_INVALID("Messages.Color.Invalid", "&c✘ &fThat is not a valid color code!"),
    COLOR_RESET("Messages.Color.Reset", "&a✔ &fYour chat color has been reset!"),
    COLOR_ONLY_PLAYERS("Messages.Color.OnlyPlayers", "&cOnly players can use this command!"),
    COMMAND_RESULT_NO_PERM("Messages.CommandResult.NoPermission", "&c✘ &fYou don't have permission for this! Permission: &c%perm%&f."),
    COMMAND_RESULT_WRONG_USAGE("Messages.CommandResult.WrongUsage", "&c✘ &fWrong usage! Please type &c%cmd% help&f!"),
    ANTI_SPAM_DENIED("Messages.AntiSpam.Denied", "&c✘ &fSpam is not allowed! Please wait another &c%time% &fseconds!"),
    PLAYER_JOIN("Messages.Player.Join", "&f● &e%prefix%%displayname%%suffix% &fjoined the game!"),
    PLAYER_JOIN_FIRST_TIME("Messages.Player.JoinFirstTime", "&f● &e%prefix%%displayname%%suffix% &fjoined the server for the first time!"),
    PLAYER_KICK("Messages.Player.Kick", "&f● &e%prefix%%displayname%%suffix% &fwas kicked from the game!"),
    PLAYER_QUIT("Messages.Player.Quit", "&f● &e%prefix%%displayname%%suffix% &fleft the game!"),
    NO_LISTENING_PLAYERS("Messages.Chat.NoOneListens", "&4⚠ &fNo players are near you to hear you talking! Try to use the global mode to chat globally!"),
    PLUGIN_ENABLED("Messages.Plugin.Enabled", "✔ ChatEx-Refresh is now enabled!"),
    PLUGIN_DISABLED("Messages.Plugin.Disabled", "✔ ChatEx-Refresh is now disabled!"),
    PLUGIN_BSTATS("Messages.Plugin.bStats", "✔ Thanks for using bStats, it was enabled! Plugin ID: %id%."),
    PLUGIN_HOOKED_LUCKPERMS("Messages.Plugin.Hooked.LuckPerms", "✔ Successfully hooked into LuckPerms!"),
    PLUGIN_HOOKED_PAPI("Messages.Plugin.Hooked.PlaceholderAPI", "✔ Successfully hooked into PlaceholderAPI!"),
    PLUGIN_NO_PERMISSIONS("Messages.Plugin.NoPermissions", "⚠ No permissions plugin found. Using default permissions."),
    RGB_LOADING("Messages.Plugin.RGB.Loading", "⚠ Loading RGB color codes."),
    RGB_NO_COLORS("Messages.Plugin.RGB.NoColors", "⚠ No custom color codes specified in config!"),
    UPDATE_AVAILABLE_CONSOLE("Messages.Update.Available.Console", "&e⚠ &fNew version of &eChatEx-Refresh &favailable on &eModrinth&f! &fCurrent version: &e%current%&f. Latest version: &e%latest%&f. Download: &e%url%&f."),
    UPDATE_UP_TO_DATE("Messages.Update.UpToDate", "&a✔ ChatEx-Refresh &fis up to date! Version: &a%current%&f."),
    UPDATE_FAILED("Messages.Update.Failed", "&e⚠ &fFailed to check for updates for &eChatEx-Refresh&f!"),
    UPDATE_NOTIFY_PLAYER("Messages.Update.Notify.Player", "&f\n&e⚠ &fNew version of &eChatEx-Refresh &favailable on &eModrinth&f!\n&f● Current version: &e%current%&f.\n&f● Latest version: &e%latest%&f.\n&f● Download: &e%url%&f.\n&f");
   
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

    public String getConsoleString() {
        return Utils.replaceColors(getStringRaw());
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