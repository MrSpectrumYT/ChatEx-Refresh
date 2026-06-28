package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColorManager {

    private static final Map<String, String> COLORS = new ConcurrentHashMap<>();
    private static final File FILE = new File(ChatEx.getInstance().getDataFolder(), "userdata.yml");
    private static YamlConfiguration config;

    public static void load() {
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(FILE);
        COLORS.clear();

        for (String nick : config.getKeys(false)) {
            String color = config.getString(nick + ".color");
            if (color != null) {
                COLORS.put(nick, color);
            }
        }
    }

    public static void save() {
        config = new YamlConfiguration();
        
        for (Map.Entry<String, String> entry : COLORS.entrySet()) {
            String nick = entry.getKey();
            String color = entry.getValue();
            config.set(nick + ".color", color);
        }

        try {
            config.save(FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRawColor(Player player) {
        if (player == null) return "";
        return COLORS.getOrDefault(player.getName(), "");
    }

    public static String getPersonalColor(Player player) {
        if (player == null) return "";
        
        String raw = COLORS.get(player.getName());
        if (raw == null || raw.isEmpty()) return "";

        String processed = raw.replace("&#", "#");
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#[0-9a-fA-F]{6}").matcher(processed);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group().substring(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);

        return result.toString().replace("&", "§");
    }

    public static void setPersonalColor(Player player, String rawColor) {
        if (player == null) return;
        COLORS.put(player.getName(), rawColor);
        save();
    }

    public static void removePersonalColor(Player player) {
        if (player == null) return;
        COLORS.remove(player.getName());
        save();
    }
    
    public static void removeByNick(String nick) {
        COLORS.remove(nick);
        save();
    }
}