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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Personal chat colors (/color). Stored by player name in userdata.yml. */
public class ColorManager {

    private static final Map<String, String> COLORS = new ConcurrentHashMap<>();
    private static final File FILE = new File(ChatEx.getInstance().getDataFolder(), "userdata.yml");
    private static final Object IO_LOCK = new Object();
    private static final Pattern TOKEN = Pattern.compile("&?#([0-9a-fA-F]{6})|&([0-9a-fA-F])|&([klmnoKLMNO])");

    private static volatile boolean loadOk = true;
    private static volatile String loadError = "";

    /** False if userdata.yml could not be read the last time. It is not overwritten in that case. */
    public static boolean isLoadOk() {
        return loadOk;
    }

    public static String getLoadError() {
        return loadError;
    }

    public static String getFileName() {
        return FILE.getName();
    }

    /**
     * Loads the database. A broken file is never overwritten: the colors that are already in memory
     * stay, and nothing is saved until the file can be read again.
     *
     * @return true if the file was read
     */
    public static boolean load() {
        synchronized (IO_LOCK) {
            ChatEx.getInstance().getDataFolder().mkdirs();
            if (!FILE.exists()) {
                try {
                    FILE.createNewFile();
                } catch (IOException e) {
                    loadOk = false;
                    loadError = Integrity.shortError(e);
                    return false;
                }
            }

            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(new String(Files.readAllBytes(FILE.toPath()), StandardCharsets.UTF_8));
            } catch (IOException | InvalidConfigurationException | RuntimeException e) {
                loadOk = false;
                loadError = Integrity.shortError(e);
                return false;
            }

            COLORS.clear();
            for (String nick : config.getKeys(false)) {
                String color = config.getString(nick + ".color");
                if (color != null) {
                    COLORS.put(nick, color);
                }
            }
            loadOk = true;
            loadError = "";
            return true;
        }
    }

    public static void save() {
        synchronized (IO_LOCK) {
            if (!loadOk) {
                Map<String, String> values = new HashMap<>();
                values.put("%file%", FILE.getName());
                Locales.INTEGRITY_DATABASE_SAVE_FAILED.send(
                    ChatEx.getInstance().getServer().getConsoleSender(), values);
                return;
            }
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<String, String> entry : COLORS.entrySet()) {
                config.set(entry.getKey() + ".color", entry.getValue());
            }
            try {
                config.save(FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getRawColor(Player player) {
        if (player == null) return "";
        return COLORS.getOrDefault(player.getName(), "");
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

    /**
     * MiniMessage tags that wrap a chat message with the personal color of the player.
     *
     * @return {open, close} or null if the player has no personal color
     */
    public static String[] wrap(Player player) {
        String raw = getRawColor(player);
        if (raw.isEmpty()) return null;

        List<String> colors = new ArrayList<>();
        Set<Character> mods = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            Matcher m = TOKEN.matcher(part);
            boolean hasColor = false;
            while (m.find()) {
                if (m.group(1) != null) {
                    if (!hasColor) colors.add("#" + m.group(1).toUpperCase());
                    hasColor = true;
                } else if (m.group(2) != null) {
                    if (!hasColor) colors.add(Utils.legacyToHex("&" + m.group(2)));
                    hasColor = true;
                } else {
                    mods.add(Character.toLowerCase(m.group(3).charAt(0)));
                }
            }
        }

        StringBuilder open = new StringBuilder();
        StringBuilder close = new StringBuilder();
        if (colors.size() >= 2) {
            open.append("<gradient:").append(String.join(":", colors)).append('>');
            close.insert(0, "</gradient>");
        } else if (colors.size() == 1) {
            open.append("<color:").append(colors.get(0)).append('>');
            close.insert(0, "</color>");
        }
        for (char c : mods) {
            String name;
            switch (c) {
                case 'l':
                    name = "bold";
                    break;
                case 'm':
                    name = "strikethrough";
                    break;
                case 'n':
                    name = "underlined";
                    break;
                case 'o':
                    name = "italic";
                    break;
                default:
                    name = "obfuscated";
                    break;
            }
            open.append('<').append(name).append('>');
            close.insert(0, "</" + name + ">");
        }
        if (open.length() == 0) return null;
        return new String[]{open.toString(), close.toString()};
    }
}
