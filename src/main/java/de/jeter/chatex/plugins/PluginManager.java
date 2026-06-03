package de.jeter.chatex.plugins;

import de.jeter.chatex.ChatEx;
import de.jeter.chatex.utils.HookManager;
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
            ChatEx.getInstance().getLogger().info("Successfully hooked into: " + handler.getName());
        } else {
            handler = new Nothing();
            ChatEx.getInstance().getLogger().info("No permissions plugin found. Using default: " + handler.getName());
        }

        if (HookManager.checkPlaceholderAPI()) {
            ChatEx.getInstance().getLogger().info("Hooked into PlaceholderAPI");
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