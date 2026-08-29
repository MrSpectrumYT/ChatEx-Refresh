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
import de.jeter.chatex.utils.adManager.AdManager;
import de.jeter.chatex.utils.adManager.SimpleAdManager;
import de.jeter.chatex.utils.adManager.SmartAdManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final AdManager adManager = Config.ADS_SMART_MANAGER.getBoolean() 
        ? new SmartAdManager() 
        : new SimpleAdManager();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("chatex.allowchat")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%perm%", "chatex.allowchat");
            player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(player, placeholders));
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();

        if (!AntiSpamManager.getInstance().isAllowed(player)) {
            long remaining = AntiSpamManager.getInstance().getRemainingSeconds(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%time%", String.valueOf(remaining));
            player.sendMessage(Locales.ANTI_SPAM_DENIED.getString(player, placeholders));
            event.setCancelled(true);
            return;
        }
        AntiSpamManager.getInstance().update(player);

        if (adManager.checkForAds(message, player)) {
            player.sendMessage(Locales.MESSAGES_AD.getString(null));
            event.setCancelled(true);
            return;
        }

        for (String blocked : Config.BLOCKED_WORDS.getStringList()) {
            if (message.toLowerCase().contains(blocked.toLowerCase())) {
                player.sendMessage(Locales.MESSAGES_BLOCKED.getString(null));
                event.setCancelled(true);
                return;
            }
        }

        String format;

        if (Config.RANGEMODE.getBoolean() && message.startsWith(Config.RANGEPREFIX.getString())) {
            if (!player.hasPermission("chatex.chat.global")) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%perm%", "chatex.chat.global");
                player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(player, placeholders));
                event.setCancelled(true);
                return;
            }
            
            message = message.replaceFirst(Pattern.quote(Config.RANGEPREFIX.getString()), "");
            format = PluginManager.getInstance().getGlobalMessageFormat(player);
        } else if (Config.RANGEMODE.getBoolean()) {
            event.getRecipients().clear();
            
            if (Utils.getLocalRecipients(player).size() == 1 && Config.SHOW_NO_RECEIVER_MSG.getBoolean()) {
                player.sendMessage(Locales.NO_LISTENING_PLAYERS.getString(player));
                event.setCancelled(true);
                return;
            }
            
            event.getRecipients().addAll(Utils.getLocalRecipients(player));
            format = PluginManager.getInstance().getMessageFormat(player);
        } else {
            format = PluginManager.getInstance().getMessageFormat(player);
        }

        format = Utils.replacePlayerPlaceholders(player, format);

        format = format.replace("%", "%%");

        format = format.replace("%%2$s", "%2$s");

        format = format.replace("%%message%%", "%2$s");

        LogHelper.debug("Final format: " + format);
        event.setFormat(format);

        String coloredMessage = Utils.translateColorCodes(message, player);
        if (coloredMessage == null) {
            event.setCancelled(true);
            return;
        }

        String rawColor = ColorManager.getRawColor(player);
        if (!rawColor.isEmpty()) {
            if (rawColor.contains(",")) {
                coloredMessage = Utils.applyGradient(coloredMessage, rawColor);
            } else {
                coloredMessage = ColorManager.getPersonalColor(player) + coloredMessage + "§r";
            }
        }

        event.setMessage(coloredMessage);
        
        if (Config.RANGEMODE.getBoolean()) {
            MentionManager.processMentionsWithRecipients(player, message, event.getRecipients());
        } else {
            MentionManager.processMentions(player, message);
        }
        
        ChatLogger.writeToFile(player, message);
    }
}