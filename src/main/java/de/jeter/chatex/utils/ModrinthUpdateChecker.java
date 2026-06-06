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
                connection.setRequestProperty("User-Agent", "ChatExRefresh/" + plugin.getDescription().getVersion());

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();

                    if (versions.size() > 0) {
                        latestVersion = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                        String currentVersion = plugin.getDescription().getVersion();

                        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                            updateAvailable = true;
                            String consoleMsg = Locales.UPDATE_AVAILABLE_CONSOLE.getConsoleString()
                                    .replace("%latest%", latestVersion)
                                    .replace("%current%", currentVersion)
                                    .replace("%url%", "https://modrinth.com/plugin/" + projectSlug);
                            Bukkit.getConsoleSender().sendMessage(consoleMsg);
                        } else {
                            String upToDate = Locales.UPDATE_UP_TO_DATE.getConsoleString()
                                    .replace("%current%", currentVersion);
                            Bukkit.getConsoleSender().sendMessage(upToDate);
                        }
                    }
                }
            } catch (Exception e) {
                String failMsg = Locales.UPDATE_FAILED.getConsoleString();
                Bukkit.getConsoleSender().sendMessage(failMsg);
            }
        });
    }

    public void notifyPlayer(Player player) {
        if (updateAvailable && player.hasPermission("chatex.notifyupdate")) {
            String playerMsg = Locales.UPDATE_NOTIFY_PLAYER.getConsoleString()
                    .replace("%latest%", latestVersion)
                    .replace("%current%", plugin.getDescription().getVersion())
                    .replace("%url%", "https://modrinth.com/plugin/" + projectSlug);
            player.sendMessage(Utils.replaceColors(playerMsg));
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}