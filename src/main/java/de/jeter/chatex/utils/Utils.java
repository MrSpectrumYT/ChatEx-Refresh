/*
 * This file is part of ChatEx Refresh
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This file is part of ChatEx
 * Copyright (C) 2022 ChatEx Team
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
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
            result = result.replaceAll("&[l-or]", "");
            result = result.replaceAll("&r", "");
        }
        
        if (hasColorWarn) {
            player.sendMessage(Locales.NO_COLOR_PERMISSION.getString(player));
        }
        if (hasHexWarn) {
            player.sendMessage(Locales.NO_HEX_PERMISSION.getString(player));
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

    public static String replaceColors(String message) {
        if (message == null) return null;
        
        message = RGBColors.translateGradientCodes(message);
        message = RGBColors.translateCustomColorCodes(message);
        message = translateHexColors(message);
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        if (Config.BLOCK_MAGIC_COLOR.getBoolean()) {
            message = message.replaceAll("(?i)§k", "");
            message = message.replaceAll("(?i)&k", "");
        }
        
        return message;
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
        return message.matches(".*&[0-9a-f].*") || message.matches(".*&[l-or].*");
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

        result = result.replace("%displayname", player.getDisplayName());
        result = result.replace("%prefix", PluginManager.getInstance().getPrefix(player));
        result = result.replace("%suffix", PluginManager.getInstance().getSuffix(player));
        result = result.replace("%player", player.getName());
        result = result.replace("%world", player.getWorld().getName());
        result = result.replace("%group", PluginManager.getInstance().getGroupNames(player).length > 0 ? PluginManager.getInstance().getGroupNames(player)[0] : "none");

        if (HookManager.checkPlaceholderAPI()) {
            LogHelper.debug("PlaceholderAPI is installed! Replacing...");
            result = PlaceholderAPI.setPlaceholders(player, result);
            LogHelper.debug("Result: " + result);
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
}