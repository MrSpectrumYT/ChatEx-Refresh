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

import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * Version gate. ChatEx-Refresh runs on Minecraft 1.16.5+ (Spigot, Paper, Folia, Purpur, ...).
 * Nothing in here touches any plugin class, so it is safe to call on very old servers.
 */
public final class ServerVersion {

    public static final int MIN_MINOR = 16;
    public static final int MIN_PATCH = 5;

    private static int major = 1;
    private static int minor = 0;
    private static int patch = 0;
    private static boolean parsed;

    private ServerVersion() {
    }

    private static void parse() {
        if (parsed) return;
        parsed = true;
        try {
            // "1.20.1-R0.1-SNAPSHOT" or the year based "26.1-R0.1-SNAPSHOT"
            String v = Bukkit.getBukkitVersion();
            int dash = v.indexOf('-');
            if (dash > 0) v = v.substring(0, dash);
            String[] parts = v.split("\\.");
            major = Integer.parseInt(parts[0].trim());
            minor = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            patch = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
        } catch (RuntimeException ex) {
            // unknown format: assume it is a new server
            major = 99;
            minor = 0;
        }
    }

    public static boolean isSupported() {
        parse();
        if (major > 1) return true;
        return minor > MIN_MINOR || (minor == MIN_MINOR && patch >= MIN_PATCH);
    }

    public static String getVersion() {
        try {
            return Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")";
        } catch (RuntimeException ex) {
            return "unknown";
        }
    }

    public static void logUnsupported(Logger log, String pluginName) {
        String line = "============================================================";
        log.severe(line);
        log.severe(" " + pluginName + " cannot start on this server!");
        log.severe(" ");
        log.severe(" Detected version : " + getVersion());
        log.severe(" Required version : Minecraft 1.16.5 or newer");
        log.severe(" Supported servers: Spigot and Paper or Paper forks");
        log.severe(" ");
        log.severe(" " + pluginName + " requires Java 17+ and Minecraft 1.16.5+.");
        log.severe(" Please update your server!");
        log.severe(" ");
        log.severe(" Plugin will be disabled!");
        log.severe(line);
    }
}
