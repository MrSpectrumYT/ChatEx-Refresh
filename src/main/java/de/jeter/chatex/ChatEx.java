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
        ColorManager.load();

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getCommand("chatex").setExecutor(new CommandHandler());
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
            
            metrics.addCustomChart(new SimplePie("used_permissions_plugin", () -> PluginManager.getInstance().getName()));
            metrics.addCustomChart(new SimplePie("updatechecker_enabled", () -> "false"));
            
            getLogger().info(Locales.PLUGIN_BSTATS.getString(null).replace("%id%", String.valueOf(pluginId)));
        }

        getLogger().info(Locales.PLUGIN_ENABLED.getString(null));
    }

    @Override
    public void onDisable() {
        AntiSpamManager.getInstance().clear();
        ChatLogger.close();
        
        if (taskManager != null) {
            taskManager.cancelAll();
        }
        
        getLogger().info(Locales.PLUGIN_DISABLED.getString(null));
    }
}