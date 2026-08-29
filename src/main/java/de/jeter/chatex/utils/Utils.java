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
import de.jeter.chatex.plugins.PluginManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-f]");
    private static final Pattern MODIFIER_PATTERN = Pattern.compile("&[l-or]");
    private static final Pattern MAGIC_PATTERN = Pattern.compile("(?i)[&§]k");

    private static final Map<Character, String> LEGACY_TO_HEX = new HashMap<>();
    static {
        LEGACY_TO_HEX.put('0', "#000000");
        LEGACY_TO_HEX.put('1', "#0000AA");
        LEGACY_TO_HEX.put('2', "#00AA00");
        LEGACY_TO_HEX.put('3', "#00AAAA");
        LEGACY_TO_HEX.put('4', "#AA0000");
        LEGACY_TO_HEX.put('5', "#AA00AA");
        LEGACY_TO_HEX.put('6', "#FFAA00");
        LEGACY_TO_HEX.put('7', "#AAAAAA");
        LEGACY_TO_HEX.put('8', "#555555");
        LEGACY_TO_HEX.put('9', "#5555FF");
        LEGACY_TO_HEX.put('a', "#55FF55");
        LEGACY_TO_HEX.put('b', "#55FFFF");
        LEGACY_TO_HEX.put('c', "#FF5555");
        LEGACY_TO_HEX.put('d', "#FF55FF");
        LEGACY_TO_HEX.put('e', "#FFFF55");
        LEGACY_TO_HEX.put('f', "#FFFFFF");
    }

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

    public static String[] splitColorAndModifiers(String part) {
        if (part == null || part.isEmpty()) return new String[]{part, ""};
        Matcher m = Pattern.compile("(&[lmnok])+$").matcher(part);
        if (m.find()) {
            String mods = m.group();
            String color = part.substring(0, part.length() - mods.length());
            return new String[]{color, mods};
        } else {
            return new String[]{part, ""};
        }
    }

    public static String legacyToHex(String token) {
        if (token == null || token.length() < 2) return null;
        if (token.charAt(0) == '&') {
            char code = token.charAt(1);
            return LEGACY_TO_HEX.get(Character.toLowerCase(code));
        }
        return null;
    }

    public static String applyGradient(String text, String colorData) {
        if (text == null || text.isEmpty() || colorData == null || colorData.isEmpty()) {
            return text;
        }

        String[] split = splitColorAndModifiers(colorData);
        String colorPart = split[0];
        String modsRaw = split[1];

        StringBuilder mods = new StringBuilder();
        for (int i = 0; i < modsRaw.length(); i++) {
            if (modsRaw.charAt(i) == '&' && i + 1 < modsRaw.length()) {
                mods.append("§").append(modsRaw.charAt(i + 1));
                i++;
            }
        }
        String modsString = mods.toString();

        String[] hexStrings = colorPart.split(",");
        if (hexStrings.length < 2) {
            String singleColor = colorPart.trim();
            if (singleColor.startsWith("&")) {
                String hex = legacyToHex(singleColor);
                if (hex != null) singleColor = hex;
            }
            return translateHexColors(singleColor) + text + "§r";
        }

        List<Color> colors = new ArrayList<>();
        for (String hex : hexStrings) {
            String clean = hex.trim();
            if (clean.startsWith("&") && clean.length() >= 2) {
                String hexVal = legacyToHex(clean);
                if (hexVal != null) clean = hexVal;
            }
            if (clean.startsWith("&#")) {
                clean = "#" + clean.substring(2);
            }
            if (!clean.startsWith("#")) {
                clean = "#" + clean;
            }
            try {
                colors.add(Color.decode(clean));
            } catch (NumberFormatException ignored) {
            }
        }

        if (colors.size() < 2) return text;

        if (text.length() == 1) {
            return ChatColor.of(colors.get(0)) + modsString + text + "§r";
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
            ))).append(modsString).append(text.charAt(i));
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