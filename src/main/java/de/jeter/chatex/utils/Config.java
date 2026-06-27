package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Config {

    B_STATS("enable-bstats", true, "Do you want to use bStats?"),
    CHECK_UPDATE("check-for-updates", true, "Check for updates on Modrinth?"),
    FORMAT("message-format", "&c[%group%] %prefix%%displayname%%suffix%: %message%", "The standard message-format."),
    GLOBALFORMAT("global-message-format", "&9[%world%] &c[%group%] %prefix%%displayname%%suffix%: &e%message%", "The message-format if ranged-mode is enabled."),
    MULTIPREFIXES("multi-prefixes", false, "Should the multi-prefixes be enabled?"),
    MULTISUFFIXES("multi-suffixes", false, "Should the multi-suffixes be enabled?"),
    RANGEMODE("ranged-mode", false, "Should the ranged-mode be enabled?"),
    RANGEPREFIX("ranged-prefix", "!", "The Prefix to use for Range Mode"),
    SHOW_NO_RECEIVER_MSG("show-no-players-near", true, "Should we check if any player would receiver your chat message?"),
    RANGE("chat-range", 100, "The range to talk to other players. Set to -1 to enable world-wide-chat"),
    MENTION_ENABLED("mention.enabled", true, "Enable player mentions with sound?"),
    MENTION_SOUND("mention.sound", "BLOCK_NOTE_BLOCK_PLING", "Sound to play when mentioned"),
    LOGCHAT("logChat", false, "Should the chat be logged?"),
    DEBUG("debug", false, "Should the debug log be enabled?"),
    PRIORITY("EventPriority", EventPriority.LOWEST.name(), "Choose the Eventpriority here of ChatEx. Listeners are called in following order: LOWEST -> LOW -> NORMAL -> HIGH -> HIGHEST -> MONITOR"),
    LOCALE("Locale", "en-EN", "Which language do you want? (You can choose betwenn az-AZ.yml, be-BY.yml, bg-BG.yml, cs-CZ.yml, da-DK.yml, de-DE.yml, el-GR.yml, es-ES.yml, fi-FI.yml, fr-FR.yml, hu-HU.yml, hy-AM.yml, it-IT.yml, ja-JP.yml, ka-GE.yml, kk-KZ.yml, ky-KG.yml, nl-NL.yml, no-NO.yml, pl-PL.yml, pt-BR.yml, ro-RO.yml, ru-RU.yml, sk-SK.yml, sr-Latn-SP.yml, sr-SP.yml, sv-SE.yml, tg-TJ.yml, th-TH.yml, tr-TR.yml, uk-UA.yml, uz-UZ.yml, vi-VN.yml, zh-CN.yml and en-EN by default.)"),
    ADS_ENABLED("Ads.Enabled", true, "Should we check for ads?"),
    ADS_BYPASS("Ads.Bypass", Arrays.asList("127.0.0.1", "my-domain.com"), "A list with allowed ips or domains."),
    ADS_LOG("Ads.Log", true, "Should the ads be logged in a file?"),
    ADS_SMART_MANAGER("Ads.SmartManager", true, "Should the \"Smart Manager\" be used? (For more information read: https://github.com/TheJeterLP/ChatEx/wiki/Ad-Manager)"),
    ADS_SMART_DOMAIN_ENDINGS("Ads.SmartConfig.DomainEndings", Arrays.asList(
            "com", "net", "org", "de", "icu", "uk", "ru", "me", "info", "top", "xyz", "tk", "cn", "ga", "cf", "nl", "eu"
    ), "The endings the SmartManager applies the multiplier to."),
    ADS_REPLACE_COMMAS("Ads.ReplaceCommas", false, "Should commas be replaced with \".\" for the add test?"),
    ADS_SMART_MULTIPLIER("Ads.SmartConfig.Multiplier", 4, "If a domain pattern contains an ending from Ads.SmartConfig.DomainEndings the score get multiplied by this number."),
    ADS_SMART_UN_MULTIPLIER("Ads.SmartConfig.UnMultiplier", 1, "If a domain pattern contains NOT an ending from Ads.SmartConfig.DomainEndings the score get multiplied by this number."),
    ADS_THRESHOLD("Ads.Threshold.Block", 0.3, "The threshold required to cancel a message."),
    ADS_REDUCE_THRESHOLD("Ads.Threshold.ReduceThreshold", 0.1, "How much threshold is removed per message"),
    ADS_MAX_LENGTH("Ads.Threshold.MaxLinkLength", 10, "What the max detected link length is (For more information read: https://github.com/TheJeterLP/ChatEx/wiki/Ad-Manager)"),
    ANTISPAM_SECONDS("AntiSpam.Seconds", 5, "The delay between player messages to prevent spam"),
    ANTISPAM_ENABLED("AntiSpam.Enable", true, "Should antispam be enabled?"),
    BLOCKED_WORDS("BlockedWords", Arrays.asList("shit", "@everyone"), "A list of words that should be blocked."),
    CHANGE_TABLIST_NAME("Tablist.Change", true, "Do you want to have the prefixes and suffixes in the tablist?"),
    TABLIST_FORMAT("Tablist.format", "%prefix%%player%%suffix%", "The format of the tablist name"),
    CHANGE_JOIN_AND_QUIT("Messages.JoinAndQuit.Enabled", false, "Do you want to change the join and the quit messages?"),
    BLOCK_MAGIC_COLOR("block-magic-color", true, "Block magic color in chat?");

    private static final File f = new File(ChatEx.getInstance().getDataFolder(), "config.yml");
    private static YamlConfiguration cfg;
    private final Object value;
    private final String path;
    private final String description;

    Config(String path, Object val, String description) {
        this.path = path;
        this.value = val;
        this.description = description;
    }

    public static void load() {
        ChatEx.getInstance().getDataFolder().mkdirs();
        reload(false);
        List<String> header = new ArrayList<>();
        header.add("Thanks for installing " + ChatEx.getInstance().getName());
        header.add("ChatEx-Refresh — a fork of the ChatEx plugin");
        header.add("Original by Jeter & Wizard_x and fork by MrSpectrumYT");
        header.add("https://modrinth.com/plugin/chatex-refresh");
        for (Config c : values()) {
            header.add(c.getPath() + ": " + c.getDescription());
            if (!cfg.contains(c.getPath())) {
                c.set(c.getDefaultValue(), false);
            }
        }
        try {
            cfg.options().setHeader(header);
        } catch (NoSuchMethodError e) {
            String headerString = "";
            for (String s : header) {
                headerString += s + System.lineSeparator();
            }
            cfg.options().header(headerString);
        }
        try {
            cfg.save(f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void reload(boolean complete) {
        if (!complete) {
            cfg = YamlConfiguration.loadConfiguration(f);
            return;
        }
        load();
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

    public String getString() {
        return Utils.replaceColors(cfg.getString(path));
    }

    public List<String> getStringList() {
        return cfg.getStringList(path);
    }

    public ConfigurationSection getConfigurationSection() {
        return cfg.getConfigurationSection(path);
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