package de.jeter.chatex;

import de.jeter.chatex.utils.Config;
import de.jeter.chatex.utils.Locales;
import de.jeter.chatex.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (Config.CHANGE_JOIN_AND_QUIT.getBoolean()) {
            Component msg;
            if (e.getPlayer().hasPlayedBefore()) {
                msg = Locales.PLAYER_JOIN.getComponent(e.getPlayer());
            } else {
                msg = Locales.PLAYER_JOIN_FIRST_TIME.getComponent(e.getPlayer());
            }
            e.joinMessage(msg);
        }

        if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
            String name = Config.TABLIST_FORMAT.getString();
            name = Utils.replacePlayerPlaceholders(e.getPlayer(), name);
            e.getPlayer().setPlayerListName(name);
        }

        if (Config.CHECK_UPDATE.getBoolean() && ChatEx.getInstance().getUpdateChecker() != null) {
            ChatEx.getInstance().getUpdateChecker().notifyPlayer(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(final PlayerQuitEvent e) {
        if (!Config.CHANGE_JOIN_AND_QUIT.getBoolean()) return;
        Component msg = Locales.PLAYER_QUIT.getComponent(e.getPlayer());
        e.quitMessage(msg);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(final PlayerKickEvent e) {
        if (!Config.CHANGE_JOIN_AND_QUIT.getBoolean()) return;
        Component msg = Locales.PLAYER_KICK.getComponent(e.getPlayer());
        e.leaveMessage(msg);
    }
}