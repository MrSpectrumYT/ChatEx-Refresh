package de.jeter.chatex.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class FoliaTaskManager {

    private final JavaPlugin plugin;
    private final boolean isFolia;

    public FoliaTaskManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFoliaAvailable();
    }

    private boolean isFoliaAvailable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void runTask(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskLater(Runnable task, long delayTicks) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void runTaskAsynchronously(Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runTaskLaterAsynchronously(Runnable task, long delayTicks) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    public void cancelAll() {
        if (!isFolia) {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}