/*
 * This file is part of ChatEx
 * Copyright (C) 2022 ChatEx Team
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import de.jeter.chatex.text.Fmt;
import de.jeter.chatex.text.Rendered;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

public enum Locales {

    HELP_MESSAGE("Messages.Help.Message",
    "&f" +
    "\n&fHelp for chat:" +
    "\n&f● <hover:show_text:'&#F34723Click to run /%cmd% clear'><click:run_command:'/%cmd% clear'>&#F34723/%cmd% clear</click></hover>&r &f— clears the chat." +
    "\n&f● <hover:show_text:'&#F34723Click to run /%cmd% help'><click:run_command:'/%cmd% help'>&#F34723/%cmd% help</click></hover>&r &f— help for chat." +
    "\n&f● <hover:show_text:'&#F34723Click to run /%cmd% reload'><click:run_command:'/%cmd% reload'>&#F34723/%cmd% reload</click></hover>&r &f— reloads the plugin." +
    "\n&f"),
    COMMAND_CLEAR_CONSOLE("Commands.Clear.Console", "CONSOLE"),
    COMMAND_CLEAR_UNKNOWN("Commands.Clear.Unknown", "UNKNOWN"),
    MESSAGES_RELOAD("Messages.Commands.Reload.Success", "&fConfiguration, locales and database successfully reloaded!"),
    MESSAGES_CLEAR("Messages.Commands.Clear.Success", "&fThe chat was cleared by &#9B2D30%clearer%&r&f!"),
    MESSAGES_AD("Messages.Chat.AdDetected", "&fAdvertising is not allowed!"),
    MESSAGES_BLOCKED("Messages.Chat.BlockedWord", "&fThis word is not allowed!"),
    MESSAGES_AD_NOTIFY("Messages.Chat.AdNotify", "&#9B2D30%player%&r &ftried to write &#9B2D30%message%&r &fin chat!"),
    NO_COLOR_PERMISSION("Messages.NoColorPermission", "&fYou don't have permission to use legacy colors!"),
    NO_MODIFIER_PERMISSION("Messages.NoModifierPermission", "&fYou don't have permission to use formatting codes!"),
    NO_HEX_PERMISSION("Messages.NoHexPermission", "&fYou don't have permission to use HEX colors!"),
    NO_GRADIENT_PERMISSION("Messages.NoGradientPermission", "&fYou don't have permission to use gradient colors!"),
    MAGIC_BLOCKED("Messages.MagicBlocked", "&fYou don't have permission to use magic color!"),
    INTEGRITY_CONFIG_OK("Messages.Integrity.Config.Loaded", "config.yml loaded successfully!"),
    INTEGRITY_CONFIG_FAILED("Messages.Integrity.Config.Failed", "config.yml is broken: %error% — the file was NOT changed! The previous/default values are used! Fix the file and use /chatex reload."),
    INTEGRITY_LOCALE_OK("Messages.Integrity.Locale.Loaded", "%file% locale loaded successfully!"),
    INTEGRITY_LOCALE_FAILED("Messages.Integrity.Locale.Failed", "%file% locale is broken: %error% — the file was NOT changed! The previous/built-in English texts are used! Fix the file and use /chatex reload."),
    INTEGRITY_DATABASE_OK("Messages.Integrity.Database.Loaded", "%file% database loaded successfully!"),
    INTEGRITY_DATABASE_FAILED("Messages.Integrity.Database.Failed", "%file% database is broken: %error% — the file was NOT changed and nothing is saved to it until it is fixed (personal colors are lost on restart)! Fix the file and use /chatex reload."),
    INTEGRITY_DATABASE_SAVE_FAILED("Messages.Integrity.Database.SaveFailed", "%file% database is broken, so the change was NOT saved to disk (the file is left untouched)! Fix the file and use /chatex reload."),
    PERSONAL_COLOR_ACTIVE("Messages.Color.PersonalActive", "&fYou have a personal chat color, so you can't use colors or text formatting in chat! Use <hover:show_text:'<#9B2D30>Click to reset your chat color'><click:run_command:'/color reset'><#9B2D30>/color reset</click></hover>&r to remove it."),
    MM_NO_COLOR("Messages.MiniMessage.NoColor", "&fYou don't have permission to use MiniMessage colors!"),
    MM_NO_MODIFIER("Messages.MiniMessage.NoModifier", "&fYou don't have permission to use MiniMessage formatting tags!"),
    MM_NO_HEX("Messages.MiniMessage.NoHex", "&fYou don't have permission to use MiniMessage HEX colors!"),
    MM_NO_MAGIC("Messages.MiniMessage.NoMagic", "&fYou don't have permission to use the MiniMessage obfuscated tag!"),
    MM_NO_GRADIENT("Messages.MiniMessage.NoGradient", "&fYou don't have permission to use the MiniMessage gradient tag!"),
    MM_NO_RAINBOW("Messages.MiniMessage.NoRainbow", "&fYou don't have permission to use the MiniMessage rainbow tag!"),
    MM_NO_URL("Messages.MiniMessage.NoUrl", "&fYou don't have permission to use clickable links in chat!"),
    MM_NO_SUGGEST("Messages.MiniMessage.NoSuggest", "&fYou don't have permission to use the suggest-command tag in chat!"),
    MM_NO_COMMAND("Messages.MiniMessage.NoCommand", "&fYou don't have permission to use the run-command tag in chat!"),
    MM_NO_COPY("Messages.MiniMessage.NoCopy", "&fYou don't have permission to use the copy-to-clipboard tag in chat!"),
    MM_NO_HOVER("Messages.MiniMessage.NoHover", "&fYou don't have permission to use the hover tag in chat!"),
    MM_NO_INSERTION("Messages.MiniMessage.NoInsertion", "&fYou don't have permission to use the insertion tag in chat!"),
    MM_NO_KEYBIND("Messages.MiniMessage.NoKeybind", "&fYou don't have permission to use the keybind tag in chat!"),
    MM_NO_FONT("Messages.MiniMessage.NoFont", "&fYou don't have permission to use the font tag in chat!"),
    MM_NO_LANG("Messages.MiniMessage.NoLang", "&fYou don't have permission to use the translatable tag in chat!"),
    MM_NO_NEWLINE("Messages.MiniMessage.NoNewline", "&fYou don't have permission to use the newline tag in chat!"),
    COLOR_USAGE("Messages.Color.Usage",
    "&f" +
    "&fUsage for colored chat:" +
    "\n&f● <hover:show_text:'&#F34723Suggest a example legacy color'><click:suggest_command:'/color &9'>&#F34723/color &#F34723&&#F34723x</click></hover>&r &f— set a legacy color." +
    "\n&f● <hover:show_text:'&#F34723Suggest an example №1 HEX color'><click:suggest_command:'/color &#FFAA00'>&#F34723/color &#F34723&&#F34723#&#F34723HEXHEX</click></hover>&r &f— set a HEX color." +
    "\n&f● <hover:show_text:'&#F34723Suggest an example №2 HEX color'><click:suggest_command:'/color #00AAFF'>&#F34723/color &#F34723#&#F34723HEXHEX</click></hover>&r &f— set a HEX color." +
    "\n&f● <hover:show_text:'&#F34723Suggest an example HEX gradient'><click:suggest_command:'/color &#FF0000,#00FF00'>&#F34723/color &#F34723#&#F34723HEX1,&#F34723&&#F34723#&#F34723HEX2,...</click></hover>&r &f— set a HEX gradient." +
    "\n&f● <hover:show_text:'&#F34723Suggest an example legacy gradient'><click:suggest_command:'/color &4,&f'>&#F34723/color &#F34723&&#F34723x1,&#F34723&&#F34723x2,...</click></hover>&r &f— set a legacy gradient." +
    "\n&f● <hover:show_text:'&#F34723Click to reset personal color'><click:run_command:'/color reset'>&#F34723/color reset</click></hover>&r &f— reset your color." +
    "\n&f" +
    "\n&fWhen setting any color, modifiers (e.g. &#F34723&&#F34723l&r&f) are placed after the color without commas or spaces; with a gradient, only after the last color!" +
    "\n&f"),
    COLOR_SET_SUCCESS("Messages.Color.Set", "&fYour chat color has been updated!"),
    COLOR_INVALID("Messages.Color.Invalid", "&fThat is not a valid color code!"),
    COLOR_RESET("Messages.Color.Reset", "&fYour chat color has been reset!"),
    COLOR_ONLY_PLAYERS("Messages.Color.OnlyPlayers", "&fOnly players can use this command!"),
    COLOR_GRADIENT_MIN_TWO("Messages.Color.GradientMinTwo", "&fGradient requires at least &#CA2C922 colors&r&f!"),
    COMMAND_RESULT_NO_PERM("Messages.CommandResult.NoPermission", "&fYou don't have permission for this! Permission: &#9B2D30%perm%&r&f."),
    COMMAND_RESULT_WRONG_USAGE("Messages.CommandResult.WrongUsage", "&fWrong usage! Please type <hover:show_text:'&#F34723Click to run /%cmd% help'><click:run_command:'/%cmd% help'>&#F34723/%cmd% help</click></hover>&r&f!"),
    ANTI_SPAM_DENIED("Messages.AntiSpam.Denied", "&fSpam is not allowed! Please wait another &#CA2C92%time% seconds&r&f!"),
    PLAYER_JOIN("Messages.Player.Join", "&f● &#6495ED%prefix%%displayname%%suffix%&r &fjoined the game!"),
    PLAYER_JOIN_FIRST_TIME("Messages.Player.JoinFirstTime", "&f● &#6495ED%prefix%%displayname%%suffix%&r &fjoined the server for the first time!"),
    PLAYER_KICK("Messages.Player.Kick", "&f● &#6495ED%prefix%%displayname%%suffix%&r &fwas kicked from the game!"),
    PLAYER_QUIT("Messages.Player.Quit", "&f● &#6495ED%prefix%%displayname%%suffix%&r &fleft the game!"),
    NO_LISTENING_PLAYERS("Messages.Chat.NoOneListens", "&fNo players are near you to hear you talking! Try to use the global mode to chat globally!"),
    PLUGIN_ENABLED("Messages.Plugin.Enabled", "ChatEx-Refresh is now enabled!"),
    PLUGIN_DISABLED("Messages.Plugin.Disabled", "ChatEx-Refresh is now disabled!"),
    PLUGIN_BSTATS("Messages.Plugin.bStats", "Thanks for using bStats, it was enabled! Plugin ID: %id%."),
    PLUGIN_HOOKED_LUCKPERMS("Messages.Plugin.Hooked.LuckPerms", "Successfully hooked into LuckPerms!"),
    PLUGIN_HOOKED_PAPI("Messages.Plugin.Hooked.PlaceholderAPI", "Successfully hooked into PlaceholderAPI!"),
    PLUGIN_NO_PERMISSIONS("Messages.Plugin.NoPermissions", "No permissions plugin found! Using default permissions."),
    UPDATE_AVAILABLE_CONSOLE("Messages.Update.Available.Console", "New version of ChatEx-Refresh available on Modrinth! Current version: %current%. Latest version: %latest%. Download: %url%."),
    UPDATE_UP_TO_DATE("Messages.Update.UpToDate", "ChatEx-Refresh is up to date! Version: %current%."),
    UPDATE_FAILED("Messages.Update.Failed", "Failed to check for updates for ChatEx-Refresh!"),
    UPDATE_NOTIFY_PLAYER("Messages.Update.Notify.Player", "&f\n&fNew version of &#3EB489ChatEx-Refresh&r &favailable on &#3EB489Modrinth&r&f!\n&f● Current version: &#3EB489%current%&r&f.\n&f● Latest version: &#3EB489%latest%&r&f.\n&f● Download: <hover:show_text:'&fClick to open our &#3EB489Modrinth&r &fpage'><click:open_url:'%url%'><#3EB489>%url%</click></hover>&r&f.\n&f");

    private static final File localeFolder = new File(ChatEx.getInstance().getDataFolder(), "locales");
    private static YamlConfiguration cfg;
    private static File f;
    private final String value;
    private final String path;

    Locales(String path, String val) {
        this.path = path;
        this.value = val;
    }

    private static volatile boolean loadOk = true;
    private static volatile String loadError = "";

    /** False if the selected locale file could not be read the last time. It is never overwritten in that case. */
    public static boolean isLoadOk() {
        return loadOk;
    }

    public static String getLoadError() {
        return loadError;
    }

    public static String getLoadedFileName() {
        return f != null ? f.getName() : Config.LOCALE.getString() + ".yml";
    }

    /**
     * Loads the selected locale. A broken file is never overwritten or reset: the previous texts
     * (or the built-in English defaults at startup) stay in use and nothing is saved until it is fixed.
     *
     * @return true if the file was read
     */
    public static boolean load() {
        localeFolder.mkdirs();
        String name = Config.LOCALE.getString();
        File file = new File(localeFolder, name + ".yml");
        f = file;

        if (!file.exists()) {
            try {
                ChatEx.getInstance().saveResource("locales/" + name + ".yml", true);
            } catch (IllegalArgumentException ex) {
                // no bundled file with that name: it is created from the built-in defaults below
            }
        }

        YamlConfiguration loaded = new YamlConfiguration();
        if (file.exists()) {
            try {
                loaded.loadFromString(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            } catch (IOException | InvalidConfigurationException | RuntimeException ex) {
                loadOk = false;
                loadError = Integrity.shortError(ex);
                if (cfg == null) {
                    cfg = new YamlConfiguration();
                }
                return false;
            }
        }

        cfg = loaded;
        loadOk = true;
        loadError = "";
        saveMissingDefaults();
        return true;
    }

    private static void saveMissingDefaults() {
        try {
            boolean changed = false;
            for (Locales c : values()) {
                if (!cfg.contains(c.getPath())) {
                    cfg.set(c.getPath(), c.getDefaultValue());
                    changed = true;
                }
            }
            if (changed || !f.exists()) {
                cfg.save(f);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public static void reload(boolean complete) {
        if (complete) {
            load();
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

    /**
     * Placeholders -> text, legacy codes -> MiniMessage, MiniMessage -> output.
     * Values of {@code additional} are inserted as plain text (they can never add colors or tags).
     */
    public Rendered render(Player p, Map<String, String> additional) {
        String text = Utils.replacePlayerPlaceholders(p, getStringRaw());
        if (additional != null) {
            for (Map.Entry<String, String> entry : additional.entrySet()) {
                text = text.replace(entry.getKey(), Fmt.escape(entry.getValue()));
            }
        }
        return Fmt.render(text);
    }

    public Rendered render(Player p) {
        return render(p, null);
    }

    /** Message as section sign string (for the console and for sendMessage). */
    public String getString(Player p) {
        return render(p, null).toLegacy();
    }

    public String getString(Player p, Map<String, String> additionalPlaceholders) {
        return render(p, additionalPlaceholders).toLegacy();
    }

    /** Message without any color/format. For logger lines. */
    public String getPlain() {
        return render(null, null).plain();
    }

    public String getPlain(Map<String, String> additionalPlaceholders) {
        return render(null, additionalPlaceholders).plain();
    }

    public String getConsoleString() {
        return getString(null);
    }

    /**
     * Sends the message.
     * <ul>
     *     <li>Players: full render, click/hover tags work.</li>
     *     <li>Console / command blocks: goes through the Bukkit logger,
     *     so every line gets the {@code [ChatEx-Refresh]} prefix automatically
     *     and no raw § or hex codes leak into the console.</li>
     * </ul>
     * An empty message is never sent.
     */
    public void send(CommandSender to) {
        send(to, Collections.<String, String>emptyMap());
    }

    public void send(CommandSender to, Map<String, String> additionalPlaceholders) {
        if (to == null) return;

        // Console / command blocks: route through Bukkit logger.
        if (!(to instanceof Player)) {
            String plain = getPlain(additionalPlaceholders);
            if (!plain.isEmpty()) {
                ChatEx.getInstance().getLogger().info(plain);
            }
            return;
        }

        Fmt.send(to, render((Player) to, additionalPlaceholders));
    }

    public void set(Object value, boolean save) {
        cfg.set(path, value);
        if (save && loadOk) {
            try {
                cfg.save(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}