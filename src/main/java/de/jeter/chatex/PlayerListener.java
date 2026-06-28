package de.jeter.chatex;

import de.jeter.chatex.utils.*;
import de.jeter.chatex.utils.adManager.SmartAdManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (Config.CHANGE_JOIN_AND_QUIT.getBoolean()) {
            String msg = event.getPlayer().hasPlayedBefore() 
                ? Locales.PLAYER_JOIN.getString(event.getPlayer())
                : Locales.PLAYER_JOIN_FIRST_TIME.getString(event.getPlayer());
            event.setJoinMessage(Utils.replacePlayerPlaceholders(event.getPlayer(), msg));
        }

        if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
            String name = Config.TABLIST_FORMAT.getString();
            name = Utils.replacePlayerPlaceholders(event.getPlayer(), name);
            event.getPlayer().setPlayerListName(name);
        }

        if (Config.CHECK_UPDATE.getBoolean() && ChatEx.getInstance().getUpdateChecker() != null) {
            ChatEx.getInstance().getUpdateChecker().notifyPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        AntiSpamManager.getInstance().remove(event.getPlayer());
        SmartAdManager.removePlayer(event.getPlayer());

        if (Config.CHANGE_JOIN_AND_QUIT.getBoolean()) {
            String msg = Locales.PLAYER_QUIT.getString(event.getPlayer());
            event.setQuitMessage(Utils.replacePlayerPlaceholders(event.getPlayer(), msg));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(PlayerKickEvent event) {
        AntiSpamManager.getInstance().remove(event.getPlayer());
        SmartAdManager.removePlayer(event.getPlayer());

        if (Config.CHANGE_JOIN_AND_QUIT.getBoolean()) {
            String msg = Locales.PLAYER_KICK.getString(event.getPlayer());
            event.setLeaveMessage(Utils.replacePlayerPlaceholders(event.getPlayer(), msg));
        }
    }
}