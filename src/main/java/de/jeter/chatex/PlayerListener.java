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