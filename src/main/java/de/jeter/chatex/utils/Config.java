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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Config {

    B_STATS("enable-bstats", true, "Do you want to use bStats?"),
    CHECK_UPDATE("check-for-updates", true, "Check for updates on Modrinth?"),
    FORMAT("message-format", "&f[&#88FF88%group%&r&f]&r &f%prefix%%displayname%%suffix%&r &#88FF88→&r %message%", "The standard message-format."),
    GLOBALFORMAT("global-message-format", "&#F9E79FⒼ&r &f[&#F9E79F%world%&r&f]&r &f[&#F9E79F%group%&r&f]&r &f%prefix%%displayname%%suffix%&r &#F9E79F→&r %message%", "The message-format if ranged-mode is enabled."),
    MULTIPREFIXES("multi-prefixes", false, "Should the multi-prefixes be enabled?"),
    MULTISUFFIXES("multi-suffixes", false, "Should the multi-suffixes be enabled?"),
    RANGEMODE("ranged-mode", true, "Should the ranged-mode be enabled?"),
    RANGEPREFIX("ranged-prefix", "!", "The Prefix to use for Range Mode"),
    SHOW_NO_RECEIVER_MSG("show-no-players-near", true, "Tell the player that nobody is near him to hear the message (ranged-mode)? If keep-message-when-no-players-near is false, the message is deleted too."),
    NO_PLAYERS_KEEP_MESSAGE("keep-message-when-no-players-near", true, "If nobody is near the player: false = the message is deleted, true = the player still sees his own message (the notice above is still shown)."),
    PERSONAL_COLOR_BLOCKS_CODES("personal-color-blocks-codes", true, "If a player has a personal color (/color), he can not use any color or text modifier in chat (&c, &l, &#hex, §c, <red>, <bold>, <gradient>, <rainbow>, <obfuscated>, <reset>); the message is blocked. Hover, click, links, newline... are still controlled by their own permissions, and colors inside the text of a hover tooltip are allowed (by their permissions)."),
    SYSTEM_MESSAGES("chat-as-system-message", false, "Send chat messages as server (system) messages instead of signed player chat packets. Removes the chat signature / 'not secure' marks. Click/hover MiniMessage tags always use this way."),
    RANGE("chat-range", 100, "The range to talk to other players. Set to -1 to enable world-wide-chat"),
    MENTION_ENABLED("mention.enabled", true, "Enable player mentions with sound?"),
    MENTION_SOUND("mention.sound", "BLOCK_NOTE_BLOCK_PLING", "Sound to play when mentioned"),
    LOGCHAT("logChat", false, "Should the chat be logged?"),
    DEBUG("debug", false, "Should the debug log be enabled?"),
    PRIORITY("EventPriority", EventPriority.LOWEST.name(), "Choose the Eventpriority here of ChatEx. Listeners are called in following order: LOWEST -> LOW -> NORMAL -> HIGH -> HIGHEST -> MONITOR"),
    LOCALE("Locale", "en-EN", "Which language do you want? (You can choose betwenn az-AZ.yml, be-BY.yml, bg-BG.yml, cs-CZ.yml, da-DK.yml, de-DE.yml, el-GR.yml, es-ES.yml, fi-FI.yml, fr-FR.yml, hu-HU.yml, hy-AM.yml, it-IT.yml, ja-JP.yml, ka-GE.yml, kk-KZ.yml, ky-KG.yml, nl-NL.yml, no-NO.yml, pl-PL.yml, pt-BR.yml, ro-RO.yml, ru-RU.yml, sk-SK.yml, sr-Latn-SP.yml, sr-SP.yml, sv-SE.yml, tg-TJ.yml, th-TH.yml, tr-TR.yml, uk-UA.yml, uz-UZ.yml, vi-VN.yml, zh-CN.yml and en-EN by default.)"),
    ADS_ENABLED("Ads.Enabled", true, "Should we check for ads?"),
    ADS_BYPASS("Ads.Bypass", Arrays.asList("127.0.0.1", "my-domain.com"), "A list with allowed ips or domains. Write [] or null for an empty list."),
    ADS_LOG("Ads.Log", true, "Should the ads be logged in a file?"),
    ADS_SMART_MANAGER("Ads.SmartManager", true, "Should the \"Smart Manager\" be used? (For more information read: https://github.com/TheJeterLP/ChatEx/wiki/Ad-Manager)"),
    ADS_SMART_DOMAIN_ENDINGS("Ads.SmartConfig.DomainEndings", Arrays.asList(
            "com", "net", "org", "de", "icu", "uk", "ru", "me", "info", "top", "xyz", "tk", "cn", "ga", "cf", "nl", "eu"
    ), "The endings the SmartManager applies the multiplier to. Write [] or null for none."),
    ADS_REPLACE_COMMAS("Ads.ReplaceCommas", false, "Should commas be replaced with \".\" for the add test?"),
    ADS_SMART_MULTIPLIER("Ads.SmartConfig.Multiplier", 4, "If a domain pattern contains an ending from Ads.SmartConfig.DomainEndings the score get multiplied by this number."),
    ADS_SMART_UN_MULTIPLIER("Ads.SmartConfig.UnMultiplier", 1, "If a domain pattern contains NOT an ending from Ads.SmartConfig.DomainEndings the score get multiplied by this number."),
    ADS_THRESHOLD("Ads.Threshold.Block", 0.3, "The threshold required to cancel a message."),
    ADS_REDUCE_THRESHOLD("Ads.Threshold.ReduceThreshold", 0.1, "How much threshold is removed per message"),
    ADS_MAX_LENGTH("Ads.Threshold.MaxLinkLength", 10, "What the max detected link length is (For more information read: https://github.com/TheJeterLP/ChatEx/wiki/Ad-Manager)"),
    ANTISPAM_SECONDS("AntiSpam.Seconds", 5, "The delay between player messages to prevent spam"),
    ANTISPAM_ENABLED("AntiSpam.Enable", true, "Should antispam be enabled?"),
    BLOCKED_WORDS("BlockedWords", Arrays.asList("shit", "@everyone"), "A list of words that should be blocked. Write [] or null to disable the word filter."),
    CHANGE_TABLIST_NAME("Tablist.Change", true, "Do you want to have the prefixes and suffixes in the tablist?"),
    TABLIST_FORMAT("Tablist.format", "%prefix%%player%%suffix%", "The format of the tablist name"),
    CHANGE_JOIN_AND_QUIT("Messages.JoinAndQuit.Enabled", false, "Do you want to change the join and the quit messages?"),
    COMMANDS_ENABLED("Commands.Enabled", true, "Should ChatEx apply its rules (colors, ad blocker, anti-spam, blocked words) to the commands listed in Commands.List?"),
    COMMANDS_LIST("Commands.List", Arrays.asList("msg", "tell", "w", "whisper", "m", "t", "pm", "r", "reply", "me"), "Commands that get the ChatEx rules. Just write the command name (aliases must be listed too), the whole text after the command is checked. Write [] or null to disable. (Optional: \"msg 1\" skips the first argument.)"),
    COMMANDS_COLORS("Commands.ApplyColors", true, "Apply the color/format permissions (chatex.chat.*) to the text of these commands?"),
    COMMANDS_ADS("Commands.ApplyAdBlocker", true, "Apply the ad blocker to the text of these commands?"),
    COMMANDS_SPAM("Commands.ApplyAntiSpam", true, "Apply the anti-spam delay (shared with the chat) to these commands?"),
    COMMANDS_WORDS("Commands.ApplyBlockedWords", true, "Apply the BlockedWords list to the text of these commands?");

    /** Options that do not exist any more. They are removed from the config.yml. */
    private static final String[] LEGACY_KEYS = {"block-magic-color"};

    private static final File f = new File(ChatEx.getInstance().getDataFolder(), "config.yml");
    private static volatile YamlConfiguration cfg;
    private final Object value;
    private final String path;
    private final String description;

    Config(String path, Object val, String description) {
        this.path = path;
        this.value = val;
        this.description = description;
    }

    private static volatile boolean loadOk = true;
    private static volatile String loadError = "";

    /** False if config.yml could not be read the last time. It is never overwritten in that case. */
    public static boolean isLoadOk() {
        return loadOk;
    }

    public static String getLoadError() {
        return loadError;
    }

    /**
     * Loads config.yml. A broken file is NEVER overwritten or reset: the previous values
     * (or the defaults at startup) stay in use and nothing is saved until the file is fixed.
     *
     * @return true if the file was read
     */
    public static boolean load() {
        ChatEx.getInstance().getDataFolder().mkdirs();

        YamlConfiguration loaded = new YamlConfiguration();
        String text = null;
        if (f.exists()) {
            try {
                text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                loaded.loadFromString(text);
            } catch (IOException | InvalidConfigurationException | RuntimeException ex) {
                loadOk = false;
                loadError = Integrity.shortError(ex);
                if (cfg == null) {
                    cfg = new YamlConfiguration();
                    fillDefaults(cfg, null);
                }
                return false;
            }
        }

        fillDefaults(loaded, text);
        for (String old : LEGACY_KEYS) {
            if (loaded.contains(old)) loaded.set(old, null);
        }

        List<String> header = new ArrayList<>();
        header.add("Thanks for installing " + ChatEx.getInstance().getName());
        header.add("ChatEx-Refresh — a fork of the ChatEx plugin. Based on the original ChatEx by TheJeterLP. Additional original contribution: Wizard_x.");
        header.add("https://modrinth.com/plugin/chatex-refresh");
        for (Config c : values()) {
            header.add(c.getPath() + ": " + c.getDescription());
        }
        String headerText = String.join(System.lineSeparator(), header) + System.lineSeparator();
        try {
            // setHeader(List) exists since 1.18.1, older servers only have header(String)
            loaded.options().getClass().getMethod("setHeader", List.class).invoke(loaded.options(), header);
        } catch (ReflectiveOperationException | RuntimeException e) {
            loaded.options().header(headerText);
        }
        try {
            loaded.save(f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        cfg = loaded;
        loadOk = true;
        loadError = "";
        return true;
    }

    /** Adds missing options. A list option that is written as null / empty in the file stays disabled. */
    private static void fillDefaults(YamlConfiguration target, String text) {
        for (Config c : values()) {
            if (target.contains(c.path)) continue;
            if (text != null && c.value instanceof List && isNullInYaml(text, c.path)) {
                target.set(c.path, new ArrayList<String>());
            } else {
                target.set(c.path, c.value);
            }
        }
    }

    private static final Pattern YAML_KEY = Pattern.compile("^(\\s*)([^\\s#:][^#:]*?)\\s*:(?:\\s+(.*?))?\\s*$");

    /** True if the key exists in the YAML text with no value (empty, null or ~). */
    static boolean isNullInYaml(String text, String path) {
        String[] lines = text.split("\r?\n", -1);
        List<Integer> indents = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) continue;
            Matcher m = YAML_KEY.matcher(line);
            if (!m.matches()) continue;
            int indent = m.group(1).length();
            while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
                indents.remove(indents.size() - 1);
                keys.remove(keys.size() - 1);
            }
            indents.add(indent);
            keys.add(m.group(2).replace("\"", "").replace("'", ""));
            if (!String.join(".", keys).equals(path)) continue;

            String value = m.group(3) == null ? "" : m.group(3).replaceAll("\\s+#.*$", "").trim();
            if (value.startsWith("#")) value = "";
            if (value.isEmpty()) {
                for (int j = i + 1; j < lines.length; j++) {
                    String next = lines[j];
                    String nt = next.trim();
                    if (nt.isEmpty() || nt.startsWith("#")) continue;
                    int nextIndent = next.length() - next.replaceAll("^\\s+", "").length();
                    return !(nextIndent > indent || nt.startsWith("-"));
                }
                return true;
            }
            return value.equalsIgnoreCase("null") || value.equals("~");
        }
        return false;
    }

    public static void reload(boolean complete) {
        if (complete) {
            load();
        }
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return value;
    }

    public boolean getBoolean() {
        return cfg.getBoolean(path);
    }

    public double getDouble() {
        return cfg.getDouble(path);
    }

    public int getInt() {
        return cfg.getInt(path);
    }

    /** The raw value. Colors are NOT converted, formats go through the text engine (see Fmt). */
    public String getString() {
        String v = cfg.getString(path);
        return v != null ? v : String.valueOf(value);
    }

    public List<String> getStringList() {
        return cfg.getStringList(path);
    }

    public ConfigurationSection getConfigurationSection() {
        return cfg.getConfigurationSection(path);
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
