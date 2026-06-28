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

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-f]");
    private static final Pattern MODIFIER_PATTERN = Pattern.compile("&[l-or]");
    private static final Pattern MAGIC_PATTERN = Pattern.compile("(?i)[&§]k");

    public static String translateColorCodes(String message, Player player) {
        if (message == null || player == null) return replaceColors(message);

        String result = message;
        boolean hasError = false;

        if (player.hasPermission("chatex.chat.colorhex")) {
            result = translateHexColors(result);
        } else if (containsHexColors(result)) {
            player.sendMessage(Locales.NO_HEX_PERMISSION.getString(player));
            result = result.replaceAll("#[A-Fa-f0-9]{6}", "");
            result = result.replaceAll("&#[A-Fa-f0-9]{6}", "");
            hasError = true;
        }

        if (player.hasPermission("chatex.chat.colorlegacy")) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        } else if (containsLegacyColors(result)) {
            player.sendMessage(Locales.NO_COLOR_PERMISSION.getString(player));
            result = result.replaceAll("&[0-9a-f]", "");
            hasError = true;
        }

        if (player.hasPermission("chatex.chat.colormodifier")) {
            result = translateModifiers(result);
        } else if (containsModifiers(result)) {
            player.sendMessage(Locales.NO_MODIFIER_PERMISSION.getString(player));
            result = result.replaceAll("&[l-or]", "");
            result = result.replaceAll("&r", "");
            hasError = true;
        }

        if (Config.BLOCK_MAGIC_COLOR.getBoolean() && !player.hasPermission("chatex.chat.magic")) {
            if (containsMagic(result)) {
                player.sendMessage(Locales.MAGIC_BLOCKED.getString(player));
                result = result.replaceAll("(?i)&k", "");
                result = result.replaceAll("(?i)§k", "");
                hasError = true;
            }
        }

        return hasError ? null : result;
    }

    private static String translateModifiers(String message) {
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
            message = message.replaceAll("(?i)[&§]k", "");
        }
        return message;
    }

    private static String translateHexColors(String message) {
        if (message == null || message.isEmpty()) return message;
        
        String processed = message.replace("&#", "#");
        Matcher matcher = HEX_PATTERN.matcher(processed);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group();
            try {
                matcher.appendReplacement(result, Matcher.quoteReplacement(ChatColor.of(hex).toString()));
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

    private static boolean containsModifiers(String message) {
        return message.matches(".*&[l-or].*");
    }

    private static boolean containsMagic(String message) {
        return message.matches(".*(?i)[&§]k.*");
    }

    public static List<Player> getLocalRecipients(Player sender) {
        List<Player> recipients = new ArrayList<>();
        int range = Config.RANGE.getInt();
        
        if (range <= 0) {
            recipients.addAll(sender.getWorld().getPlayers());
            return recipients;
        }

        double squaredDistance = Math.pow(range, 2);
        Location senderLoc = sender.getLocation();

        for (Player recipient : sender.getWorld().getPlayers()) {
            if (senderLoc.distanceSquared(recipient.getLocation()) <= squaredDistance) {
                recipients.add(recipient);
            }
        }
        return recipients;
    }

    public static String replacePlayerPlaceholders(Player player, String format) {
        if (player == null || format == null) return format;

        String result = format
            .replace("%displayname%", player.getDisplayName())
            .replace("%prefix%", PluginManager.getInstance().getPrefix(player))
            .replace("%suffix%", PluginManager.getInstance().getSuffix(player))
            .replace("%player%", player.getName())
            .replace("%world%", player.getWorld().getName());

        String[] groups = PluginManager.getInstance().getGroupNames(player);
        result = result.replace("%group%", groups.length > 0 ? groups[0] : "none");

        if (HookManager.checkPlaceholderAPI()) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        return replaceColors(result);
    }

    public static String escape(String string) {
        return string != null ? string.replace("%", "%%") : null;
    }

    public static boolean checkForBypassString(String message) {
        for (String bypass : Config.ADS_BYPASS.getStringList()) {
            if (message.toLowerCase().contains(bypass.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void notifyOps(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("chatex.notifyad")) {
                player.sendMessage(message);
            }
        }
    }

    public static String applyGradient(String text, String colorData) {
        if (text == null || text.isEmpty() || colorData == null || colorData.isEmpty()) {
            return text;
        }

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

        String[] hexStrings = colorsPart.replace("&#", "#").replace("&", "").split(",");
        if (hexStrings.length < 2) {
            return translateHexColors(colorsPart) + text + "§r";
        }

        List<Color> colors = new ArrayList<>();
        for (String hex : hexStrings) {
            String clean = hex.trim();
            if (!clean.startsWith("#")) {
                clean = "#" + clean;
            }
            try {
                colors.add(Color.decode(clean));
            } catch (NumberFormatException ignored) {}
        }

        if (colors.size() < 2) return text;

        StringBuilder modifiers = new StringBuilder();
        for (char c : modifiersPart.toCharArray()) {
            modifiers.append("§").append(c);
        }
        String mods = modifiers.toString();

        if (text.length() == 1) {
            return ChatColor.of(colors.get(0)) + mods + text + "§r";
        }

        StringBuilder result = new StringBuilder();
        int textLen = text.length();
        int sections = colors.size() - 1;
        double interval = (double) (textLen - 1) / sections;

        for (int i = 0; i < textLen; i++) {
            int section = Math.min((int) (i / interval), sections - 1);
            double progress = (i - section * interval) / interval;

            Color c1 = colors.get(section);
            Color c2 = colors.get(section + 1);

            int r = (int) (c1.getRed() + progress * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + progress * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + progress * (c2.getBlue() - c1.getBlue()));

            result.append(ChatColor.of(new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
            ))).append(mods).append(text.charAt(i));
        }

        return result + "§r";
    }

    public static void log(String message) {
        if (message == null) return;
        Bukkit.getConsoleSender().sendMessage("§7[§aChatEx-Refresh§7] §f" + replaceColors(message));
    }

    public static void warn(String message) {
        if (message == null) return;
        Bukkit.getConsoleSender().sendMessage("§7[§aChatEx-Refresh§7] §e" + replaceColors(message));
    }
}