package de.jeter.chatex.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.jeter.chatex.ChatEx;
import org.bukkit.Bukkit;
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
    private String latestDownloadUrl = null;

    public ModrinthUpdateChecker(ChatEx plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
    }

    public void checkForUpdates() {
        if (!Config.CHECK_UPDATE.getBoolean()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "ChatExRefresh/" + plugin.getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();

                        if (!versions.isEmpty()) {
                            var latestVersionObj = versions.get(0).getAsJsonObject();
                            latestVersion = latestVersionObj.get("version_number").getAsString();
                            JsonArray files = latestVersionObj.get("files").getAsJsonArray();
                            if (!files.isEmpty()) {
                                latestDownloadUrl = files.get(0).getAsJsonObject().get("url").getAsString();
                            }

                            String currentVersion = plugin.getDescription().getVersion();

                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                                updateAvailable = true;
                                String consoleMsg = Locales.UPDATE_AVAILABLE_CONSOLE.getString(null)
                                        .replace("%latest%", latestVersion)
                                        .replace("%current%", currentVersion)
                                        .replace("%url%", "https://modrinth.com/plugin/" + projectSlug);
                                Bukkit.getConsoleSender().sendMessage(consoleMsg);
                            } else {
                                String upToDate = Locales.UPDATE_UP_TO_DATE.getString(null)
                                        .replace("%current%", currentVersion);
                                Bukkit.getConsoleSender().sendMessage(upToDate);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (Config.DEBUG.getBoolean()) {
                    e.printStackTrace();
                }
                String failMsg = Locales.UPDATE_FAILED.getString(null);
                Bukkit.getConsoleSender().sendMessage(failMsg);
            }
        });
    }

    public void notifyPlayer(Player player) {
        if (!updateAvailable || player == null) return;
        
        if (player.hasPermission("chatex.notifyupdate")) {
            String playerMsg = Locales.UPDATE_NOTIFY_PLAYER.getString(player)
                    .replace("%latest%", latestVersion)
                    .replace("%current%", plugin.getDescription().getVersion())
                    .replace("%url%", "https://modrinth.com/plugin/" + projectSlug);
            player.sendMessage(Utils.replaceColors(playerMsg));
        }
    }

    public void notifyAllPlayers() {
        if (!updateAvailable) return;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            notifyPlayer(player);
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

    public void forceCheck() {
        updateAvailable = false;
        latestVersion = null;
        checkForUpdates();
    }
}