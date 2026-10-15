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
package de.jeter.chatex;

import de.jeter.chatex.plugins.PluginManager;
import de.jeter.chatex.utils.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ChatEx extends JavaPlugin {

    private static ChatEx INSTANCE;
    private FoliaTaskManager taskManager;
    private ModrinthUpdateChecker updateChecker;
    private ChatListener chatListener;
    private boolean started;

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

        // must be the very first thing: on old servers nothing else of the plugin may be touched
        if (!ServerVersion.isSupported()) {
            ServerVersion.logUnsupported(getLogger(), getDescription().getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            startup();
            started = true;
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "ChatEx-Refresh could not be started on " + ServerVersion.getVersion()
                    + ". Please report this error. The plugin will be disabled.", t);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void startup() {
        taskManager = new FoliaTaskManager(this);

        Config.load();
        Locales.load();
        DomainDictionary.reload();
        CommandFilterListener.rebuild();
        PluginManager.load();
        ChatLogger.load();
        ColorManager.load();
        Integrity.report(getLogger());

        chatListener = new ChatListener();
        chatListener.register(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new CommandFilterListener(), this);

        CommandHandler handler = new CommandHandler();
        getCommand("chatex").setExecutor(handler);
        getCommand("chatex").setTabCompleter(handler);

        ColorCommand colorCmd = new ColorCommand();
        getCommand("color").setExecutor(colorCmd);
        getCommand("color").setTabCompleter(colorCmd);

        if (Config.CHECK_UPDATE.getBoolean()) {
            updateChecker = new ModrinthUpdateChecker(this, "chatex-refresh");
            updateChecker.checkForUpdates();
        }

        if (Config.B_STATS.getBoolean()) {
            int pluginId = 31278;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new SimplePie("used_permissions_plugin",
                    () -> PluginManager.getInstance().getName()));
            metrics.addCustomChart(new SimplePie("updatechecker_enabled",
                    () -> String.valueOf(Config.CHECK_UPDATE.getBoolean())));

            getLogger().info(Locales.PLUGIN_BSTATS.getPlain().replace("%id%", String.valueOf(pluginId)));
        }

        getLogger().info(Locales.PLUGIN_ENABLED.getPlain());
    }

    /**
     * /chatex reload. A file that can not be read is never reset: the previous values stay in use.
     *
     * @return true if config.yml, the locale and the database were all read
     */
    public boolean reloadAll() {
        Config.reload(true);
        Locales.fullReload();
        DomainDictionary.reload();
        CommandFilterListener.rebuild();
        ColorManager.load();
        ChatLogger.load();
        Integrity.report(getLogger());

        if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                taskManager.runForEntity(p, () -> p.setPlayerListName(Utils.tablistName(p)));
            }
        }
        return Integrity.allOk();
    }

    @Override
    public void onDisable() {
        if (started) {
            AntiSpamManager.getInstance().clear();
            ChatLogger.close();
            if (chatListener != null) chatListener.clear();
            if (taskManager != null) taskManager.cancelAll();
            getLogger().info(Locales.PLUGIN_DISABLED.getPlain());
        }
        started = false;
        INSTANCE = null;
    }
}
