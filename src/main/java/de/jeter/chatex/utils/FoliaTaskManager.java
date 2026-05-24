/*
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package de.jeter.chatex.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FoliaTaskManager {

    private final JavaPlugin plugin;
    private final boolean isFolia;
    private final Set<Runnable> runningTasks = new HashSet<>();

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

    private Player getAnyPlayer() {
        return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
    }

    public void runTask(Runnable task) {
        if (isFolia) {
            Player player = getAnyPlayer();
            if (player != null) {
                player.getScheduler().run(plugin, scheduledTask -> task.run(), null);
            } else {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskLater(Runnable task, long delayTicks) {
        if (isFolia) {
            Player player = getAnyPlayer();
            if (player != null) {
                player.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delayTicks);
            } else {
                task.run();
            }
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
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
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