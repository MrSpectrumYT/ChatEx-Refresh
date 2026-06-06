package de.jeter.chatex.plugins;

import de.jeter.chatex.ChatEx;
import de.jeter.chatex.utils.HookManager;
import de.jeter.chatex.utils.Locales;
import de.jeter.chatex.utils.Utils;

import org.bukkit.entity.Player;

public class PluginManager implements PermissionsPlugin {

    private static PermissionsPlugin handler;
    private static PluginManager INSTANCE;

    public static PermissionsPlugin getInstance() {
        return INSTANCE;
    }

    public static void load() {
        INSTANCE = new PluginManager();
        if (HookManager.checkLuckperms()) {
            handler = new LuckPerms();
            ChatEx.getInstance().getLogger().info(Locales.PLUGIN_HOOKED_LUCKPERMS.getString(null));
        } else {
            handler = new Nothing();
            ChatEx.getInstance().getLogger().info(Locales.PLUGIN_NO_PERMISSIONS.getString(null));
        }

        if (HookManager.checkPlaceholderAPI()) {
            ChatEx.getInstance().getLogger().info(Locales.PLUGIN_HOOKED_PAPI.getString(null));
        }
    }

    @Override
    public String getName() {
        return handler.getName();
    }

    @Override
    public String getPrefix(Player p) {
        return handler.getPrefix(p);
    }

    @Override
    public String getSuffix(Player p) {
        return handler.getSuffix(p);
    }

    @Override
    public String[] getGroupNames(Player p) {
        return handler.getGroupNames(p);
    }

    @Override
    public String getMessageFormat(Player p) {
        return handler.getMessageFormat(p);
    }

    @Override
    public String getGlobalMessageFormat(Player p) {
        return handler.getGlobalMessageFormat(p);
    }
}