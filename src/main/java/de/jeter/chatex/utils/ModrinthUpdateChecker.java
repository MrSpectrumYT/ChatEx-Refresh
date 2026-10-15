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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.jeter.chatex.ChatEx;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModrinthUpdateChecker {

    private final ChatEx plugin;
    private final String projectSlug;
    private volatile String latestVersion = null;
    private volatile boolean updateAvailable = false;
    private volatile String latestDownloadUrl = null;

    public ModrinthUpdateChecker(ChatEx plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
    }

    private Map<String, String> values(String current) {
        Map<String, String> values = new HashMap<>();
        values.put("%current%", current);
        values.put("%latest%", String.valueOf(latestVersion));
        values.put("%url%", "https://modrinth.com/plugin/" + projectSlug);
        return values;
    }

    public void checkForUpdates() {
        if (!Config.CHECK_UPDATE.getBoolean()) {
            return;
        }

        plugin.getTaskManager().runTaskAsynchronously(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) URI.create("https://api.modrinth.com/v2/project/" + projectSlug + "/version").toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "ChatExRefresh/" + plugin.getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonArray versions = new JsonParser().parse(reader).getAsJsonArray();

                        if (versions.size() > 0) {
                            JsonObject latest = versions.get(0).getAsJsonObject();
                            latestVersion = latest.get("version_number").getAsString();
                            JsonArray files = latest.get("files").getAsJsonArray();
                            if (files.size() > 0) {
                                latestDownloadUrl = files.get(0).getAsJsonObject().get("url").getAsString();
                            }

                            String currentVersion = plugin.getDescription().getVersion();
                            if (isNewer(latestVersion, currentVersion)) {
                                updateAvailable = true;
                                Locales.UPDATE_AVAILABLE_CONSOLE.send(Bukkit.getConsoleSender(), values(currentVersion));
                            } else {
                                Locales.UPDATE_UP_TO_DATE.send(Bukkit.getConsoleSender(), values(currentVersion));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (Config.DEBUG.getBoolean()) {
                    e.printStackTrace();
                }
                Locales.UPDATE_FAILED.send(Bukkit.getConsoleSender());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    /**
     * True if {@code latest} is a newer version than {@code current}. Numbers are compared,
     * and a pre-release (5.0.0.b1, 5.0.0-B1) is older than the release 5.0.0.
     * A test build is never told to "update" to an older release.
     */
    static boolean isNewer(String latest, String current) {
        int[] a = numbers(latest);
        int[] b = numbers(current);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        boolean latestPre = isPreRelease(latest);
        boolean currentPre = isPreRelease(current);
        if (latestPre != currentPre) return !latestPre;
        return !latest.equalsIgnoreCase(current);
    }

    private static boolean isPreRelease(String v) {
        String l = v.toLowerCase();
        return l.contains("b") || l.contains("alpha") || l.contains("rc") || l.contains("snapshot") || l.contains("dev");
    }

    /** The leading numbers of a version, up to the first pre-release marker. */
    private static int[] numbers(String v) {
        String base = v.toLowerCase().split("[-+_]|\\.?(alpha|beta|b|rc|snapshot|dev)\\d*")[0];
        String[] parts = base.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].replaceAll("\\D", ""));
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    public void notifyPlayer(Player player) {
        if (!updateAvailable || player == null) return;

        if (player.hasPermission("chatex.notifyupdate")) {
            Locales.UPDATE_NOTIFY_PLAYER.send(player, values(plugin.getDescription().getVersion()));
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getLatestDownloadUrl() {
        return latestDownloadUrl;
    }
}
