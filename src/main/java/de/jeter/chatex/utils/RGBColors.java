package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RGBColors {

    private static final HashMap<String, String> placeHolderColorMap = new HashMap<>();

    public static void load() {
        ChatEx.getInstance().getLogger().info("Loading RGB color codes...");
        
        if (Config.RGB_COLORS.getConfigurationSection() == null) {
            ChatEx.getInstance().getLogger().info("No custom color codes specified in config!");
            return;
        }

        for (Map.Entry<String, Object> stringObjectEntry : Config.RGB_COLORS.getConfigurationSection().getValues(false).entrySet()) {
            String key = stringObjectEntry.getKey();
            String value = (String) stringObjectEntry.getValue();
            LogHelper.debug("Loading custom color code " + key + " with value " + value + " from config!");
            
            if (value == null || value.isEmpty()) continue;
            
            String clearedValue = value.replaceFirst("#", "");
            char[] valueChars = clearedValue.toCharArray();
            StringBuilder rgbColor = new StringBuilder();
            rgbColor.append("§x");
            for (char c : valueChars) {
                rgbColor.append("§").append(c);
            }
            LogHelper.debug("Putting KEY: " + key + " value: " + rgbColor);
            placeHolderColorMap.put(key, rgbColor.toString());
        }
    }

    public static String translateCustomColorCodes(String s) {
        if (s == null) return s;
        
        for (Map.Entry<String, String> stringColorEntry : placeHolderColorMap.entrySet()) {
            s = s.replace(stringColorEntry.getKey(), stringColorEntry.getValue());
        }
        return s;
    }

    public static String translateGradientCodes(String message) {
        if (message == null) return message;
        
        final Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            String replacement = ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5);
            matcher.appendReplacement(buffer, replacement);
        }
        return matcher.appendTail(buffer).toString();
    }
}