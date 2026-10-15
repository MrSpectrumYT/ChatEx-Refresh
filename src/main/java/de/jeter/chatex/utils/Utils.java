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

import de.jeter.chatex.plugins.PluginManager;
import de.jeter.chatex.text.Fmt;
import de.jeter.chatex.text.MiniParser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern TRAILING_MODIFIERS = Pattern.compile("(&[lmnok])+$", Pattern.CASE_INSENSITIVE);

    /** Players that can hear {@code sender} (same world, inside the chat range). Includes the sender. */
    public static List<Player> getLocalRecipients(Player sender) {
        List<Player> recipients = new ArrayList<>();
        int range = Config.RANGE.getInt();
        World world = sender.getWorld();

        if (range <= 0) {
            recipients.addAll(world.getPlayers());
            return recipients;
        }

        double squaredDistance = (double) range * range;
        Location senderLoc = sender.getLocation();

        for (Player recipient : world.getPlayers()) {
            if (recipient == sender) {
                recipients.add(recipient);
                continue;
            }
            Location loc = recipient.getLocation();
            if (loc.getWorld() == senderLoc.getWorld() && senderLoc.distanceSquared(loc) <= squaredDistance) {
                recipients.add(recipient);
            }
        }
        return recipients;
    }

    /**
     * Replaces the placeholders of ChatEx and PlaceholderAPI. The result is raw text:
     * colors are not converted here (see {@link Fmt}).
     */
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
        return result;
    }

    /** The tablist name of a player as section sign string. */
    public static String tablistName(Player player) {
        return Fmt.legacy(replacePlayerPlaceholders(player, Config.TABLIST_FORMAT.getString()));
    }

    public static boolean checkForBypassString(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        for (String bypass : Config.ADS_BYPASS.getStringList()) {
            if (lower.contains(bypass.toLowerCase(Locale.ROOT))) {
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

    /** Splits "#FF0000&amp;l&amp;o" into the color part and the trailing modifiers. */
    public static String[] splitColorAndModifiers(String part) {
        if (part == null || part.isEmpty()) return new String[]{part, ""};
        Matcher m = TRAILING_MODIFIERS.matcher(part);
        if (m.find()) {
            String mods = m.group();
            return new String[]{part.substring(0, part.length() - mods.length()), mods};
        }
        return new String[]{part, ""};
    }

    /** "&amp;c" -> "#FF5555", otherwise null. */
    public static String legacyToHex(String token) {
        if (token == null || token.length() < 2 || token.charAt(0) != '&') return null;
        int idx = Character.digit(token.charAt(1), 16);
        return idx >= 0 ? MiniParser.LEGACY_HEX[idx] : null;
    }
}
