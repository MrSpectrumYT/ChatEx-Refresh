/*
 * This file is part of ChatEx Refresh
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This file is part of ChatEx
 * Copyright (C) 2022 ChatEx Team
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
package de.jeter.chatex;

import de.jeter.chatex.plugins.PluginManager;
import de.jeter.chatex.utils.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatEx extends JavaPlugin {

    private static ChatEx INSTANCE;
    private FoliaTaskManager taskManager;
    private ModrinthUpdateChecker updateChecker;

    public static ChatEx getInstance() {
        return INSTANCE;
    }

    public FoliaTaskManager getTaskManager() {
        return taskManager;
    }

    public ModrinthUpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        taskManager = new FoliaTaskManager(this);

        Config.load();
        Locales.load();
        PluginManager.load();
        ChatLogger.load();
        RGBColors.load();

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getCommand("chatex").setExecutor(new CommandHandler());

        if (Config.CHECK_UPDATE.getBoolean()) {
            updateChecker = new ModrinthUpdateChecker(this, "chatex-refresh");
            updateChecker.checkForUpdates();
        }

        if (Config.B_STATS.getBoolean()) {
            int pluginId = 31278;
            Metrics metrics = new Metrics(this, pluginId);
            
            metrics.addCustomChart(new SimplePie("used_permissions_plugin", () -> PluginManager.getInstance().getName()));
            metrics.addCustomChart(new SimplePie("updatechecker_enabled", () -> "false"));
            
            getLogger().info("Thanks for using bStats, it was enabled! (Plugin ID: " + pluginId + ")");
        }

        getLogger().info("ChatEx-Refresh is now enabled!");
    }

    @Override
    public void onDisable() {
        AntiSpamManager.getInstance().clear();
        ChatLogger.close();
        
        if (taskManager != null) {
            taskManager.cancelAll();
        }
        
        getLogger().info("ChatEx-Refresh is now disabled!");
    }
}