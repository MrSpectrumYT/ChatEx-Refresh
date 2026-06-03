package de.jeter.chatex.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class HookManager {

    public static boolean checkLuckperms() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("LuckPerms");
        return plugin != null && plugin.isEnabled();
    }

    public static boolean checkPlaceholderAPI() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return plugin != null && plugin.isEnabled();
    }
}
