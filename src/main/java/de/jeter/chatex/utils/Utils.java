package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import de.jeter.chatex.plugins.PluginManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String translateColorCodes(String string, Player player) {
        if (player == null) return replaceColors(string);
        
        String result = string;
        boolean hasColorWarn = false;
        boolean hasHexWarn = false;
        boolean hasModifierWarn = false;
        boolean shouldCancel = false;
        
        if (player.hasPermission("chatex.chat.colorhex")) {
            result = translateHexColors(result);
        } else {
            if (containsHexColors(result)) {
                hasHexWarn = true;
                shouldCancel = true;
            }
            result = result.replaceAll("#[A-Fa-f0-9]{6}", "");
            result = result.replaceAll("&#[A-Fa-f0-9]{6}", "");
        }
        
        if (player.hasPermission("chatex.chat.colorlegacy")) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        } else {
            if (containsLegacyColors(result)) {
                hasColorWarn = true;
                shouldCancel = true;
            }
            result = result.replaceAll("&[0-9a-f]", "");
        }
        
        if (player.hasPermission("chatex.chat.colormodifier")) {
            result = translateModifierColors(result);
        } else {
            if (containsModifierColors(result)) {
                hasModifierWarn = true;
                shouldCancel = true;
            }
            result = result.replaceAll("&[l-or]", "");
            result = result.replaceAll("&r", "");
        }
        
        if (hasColorWarn) {
            player.sendMessage(Locales.NO_COLOR_PERMISSION.getString(player));
        }
        if (hasHexWarn) {
            player.sendMessage(Locales.NO_HEX_PERMISSION.getString(player));
        }
        if (hasModifierWarn) {
            player.sendMessage(Locales.NO_MODIFIER_PERMISSION.getString(player));
        }

        if (Config.BLOCK_MAGIC_COLOR.getBoolean() && !player.hasPermission("chatex.chat.magic")) {
            if (containsMagicColor(result)) {
                player.sendMessage(Locales.MAGIC_BLOCKED.getString(player));
                shouldCancel = true;
                result = result.replaceAll("(?i)&k", "");
                result = result.replaceAll("(?i)§k", "");
            }
        }
        
        if (shouldCancel) {
            return null;
        }
        
        return result;
    }

    private static String translateModifierColors(String message) {
        return message
                .replace("&l", "§l")
                .replace("&m", "§m")
                .replace("&n", "§n")
                .replace("&o", "§o")
                .replace("&r", "§r");
    }

    public static String replaceColors(String message) {
        if (message == null) return null;
        message = translateHexColors(message);
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        if (Config.BLOCK_MAGIC_COLOR.getBoolean()) {
            message = message.replaceAll("(?i)§k", "");
            message = message.replaceAll("(?i)&k", "");
        }
        return message;
    }
    
    public static void log(String message) {
        if (message == null) return;
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.getConsoleSender().sendMessage("§7[§aChatEx-Refresh§7] §f" + colored);
    }

    public static void warn(String message) {
        if (message == null) return;
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.getConsoleSender().sendMessage("§7[§aChatEx-Refresh§7] §e" + colored);
    }

    private static String translateHexColors(String message) {
        if (message == null || message.isEmpty()) return message;
        
        String processed = message.replace("&#", "#");
        Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = hexPattern.matcher(processed);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group();
            try {
                String colorCode = ChatColor.of(hex).toString();
                matcher.appendReplacement(result, Matcher.quoteReplacement(colorCode));
            } catch (IllegalArgumentException e) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(hex));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static boolean containsHexColors(String message) {
        return message.matches(".*#[A-Fa-f0-9]{6}.*") || message.matches(".*&#[A-Fa-f0-9]{6}.*");
    }
    
    private static boolean containsLegacyColors(String message) {
        return message.matches(".*&[0-9a-f].*");
    }
    
    private static boolean containsModifierColors(String message) {
        return message.matches(".*&[l-or].*");
    }
    
    private static boolean containsMagicColor(String message) {
        return message.matches(".*(?i)&k.*") || message.matches(".*(?i)§k.*");
    }

    public static List<Player> getLocalRecipients(Player sender) {
        Location playerLocation = sender.getLocation();
        List<Player> recipients = new ArrayList<>();

        double squaredDistance = Math.pow(Config.RANGE.getInt(), 2);
        for (Player recipient : sender.getWorld().getPlayers()) {
            if (Config.RANGE.getInt() > 0 && (playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance)) {
                continue;
            }
            recipients.add(recipient);
        }
        return recipients;
    }

    public static String replacePlayerPlaceholders(Player player, String format) {
        if (player == null) {
            return format;
        }
        String result = format;

        result = result.replace("%displayname%", player.getDisplayName());
        result = result.replace("%prefix%", PluginManager.getInstance().getPrefix(player));
        result = result.replace("%suffix%", PluginManager.getInstance().getSuffix(player));
        result = result.replace("%player%", player.getName());
        result = result.replace("%world%", player.getWorld().getName());
        result = result.replace("%group%", PluginManager.getInstance().getGroupNames(player).length > 0 
            ? PluginManager.getInstance().getGroupNames(player)[0] : "none");

        if (HookManager.checkPlaceholderAPI()) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        result = replaceColors(result);
        return result;
    }

    public static String escape(String string) {
        return string.replace("%", "%%");
    }

    public static boolean checkForBypassString(String message) {
        for (String block : Config.ADS_BYPASS.getStringList()) {
            if (message.toLowerCase().contains(block.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void notifyOps(String msg) {
        for (Player op : ChatEx.getInstance().getServer().getOnlinePlayers()) {
            if (!op.hasPermission("chatex.notifyad")) {
                continue;
            }
            op.sendMessage(msg);
        }
    }

    public static String applyGradient(String text, String colorData) {
        if (text == null || text.isEmpty()) return text;
        if (colorData == null || colorData.isEmpty()) return text;

        String colorsPart = colorData;
        String modifiersPart = "";
        
        int lastAmp = colorData.lastIndexOf('&');
        if (lastAmp > 0 && lastAmp < colorData.length() - 1) {
            String possibleMods = colorData.substring(lastAmp + 1);
            if (possibleMods.matches("[lmnork]+")) {
                colorsPart = colorData.substring(0, lastAmp);
                modifiersPart = possibleMods;
            }
        }

        String colorsCleaned = colorsPart.replace("&#", "#").replace("&", "");
        String[] hexColors = colorsCleaned.split(",");
        
        if (hexColors.length < 2) {
            return translateHexColors(colorsPart) + text + "§r";
        }

        List<Color> colors = new ArrayList<>();
        for (String hex : hexColors) {
            String cleanHex = hex.trim();
            if (!cleanHex.startsWith("#")) {
                cleanHex = "#" + cleanHex;
            }
            try {
                colors.add(Color.decode(cleanHex));
            } catch (NumberFormatException e) {
            }
        }

        if (colors.size() < 2) return text;
        if (text.length() == 1) return ChatColor.of(colors.get(0)) + text + "§r";

        StringBuilder modsBuilder = new StringBuilder();
        if (!modifiersPart.isEmpty()) {
            for (char c : modifiersPart.toCharArray()) {
                modsBuilder.append("§").append(c);
            }
        }
        String formattedMods = modsBuilder.toString();

        StringBuilder gradientBuilder = new StringBuilder();
        int textLength = text.length();
        int sections = colors.size() - 1;
        double interval = (double) (textLength - 1) / sections;

        if (interval <= 0) interval = 1;

        for (int i = 0; i < textLength; i++) {
            int currentSection = (int) (i / interval);
            if (currentSection >= sections) currentSection = sections - 1;

            double sectionProgress = (i - (currentSection * interval)) / interval;

            Color c1 = colors.get(currentSection);
            Color c2 = colors.get(currentSection + 1);

            int r = (int) (c1.getRed() + sectionProgress * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + sectionProgress * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + sectionProgress * (c2.getBlue() - c1.getBlue()));

            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));

            ChatColor chatColor = ChatColor.of(new Color(r, g, b));
            gradientBuilder.append(chatColor.toString()).append(formattedMods).append(text.charAt(i));
        }

        return gradientBuilder.toString() + "§r";
    }
}