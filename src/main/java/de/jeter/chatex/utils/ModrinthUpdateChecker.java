/*
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package de.jeter.chatex.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.jeter.chatex.ChatEx;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ModrinthUpdateChecker {

    private final ChatEx plugin;
    private final String projectSlug;
    private String latestVersion = null;
    private boolean updateAvailable = false;

    public ModrinthUpdateChecker(ChatEx plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
    }

    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "ChatExRefresh/4.0.0");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();

                    if (versions.size() > 0) {
                        latestVersion = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                        String currentVersion = plugin.getDescription().getVersion();

                        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                            updateAvailable = true;
                            plugin.getLogger().warning("==========================================");
                            plugin.getLogger().warning("New version available: " + latestVersion);
                            plugin.getLogger().warning("Current version: " + currentVersion);
                            plugin.getLogger().warning("Download: https://modrinth.com/plugin/" + projectSlug);
                            plugin.getLogger().warning("==========================================");
                        } else {
                            plugin.getLogger().info("ChatEx Refresh is up to date! (v" + currentVersion + ")");
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    public void notifyPlayer(Player player) {
        if (updateAvailable && player.hasPermission("chatex.notifyupdate")) {
            player.sendMessage(Utils.replaceColors("&a✔ &fNew version &a" + latestVersion + " &favailable on &aModrinth&f!"));
            player.sendMessage(Utils.replaceColors("&a✔ &fDownload: &ahttps://modrinth.com/plugin/" + projectSlug));
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}