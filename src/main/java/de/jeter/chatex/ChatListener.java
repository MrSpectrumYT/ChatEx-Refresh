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
import de.jeter.chatex.text.Fmt;
import de.jeter.chatex.text.MessageGuard;
import de.jeter.chatex.text.MiniParser;
import de.jeter.chatex.text.Rendered;
import de.jeter.chatex.utils.*;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Chat handling.
 * <p>
 * Normal way: the event format / message are set (works with every other plugin).
 * Component way (system messages, click/hover): the event is cancelled at MONITOR and the line
 * is sent to the recipients as a server message. Doing it at MONITOR keeps mute / vanish plugins working.
 */
public class ChatListener implements Listener {

    /** Marks the place of the message while the format is rendered. */
    private static final String SENTINEL = "\uE000";

    private static final class Pending {
        final String formatMM;
        final String messageMM;
        final String legacyFormat;
        final String legacyMessage;

        Pending(String formatMM, String messageMM, String legacyFormat, String legacyMessage) {
            this.formatMM = formatMM;
            this.messageMM = messageMM;
            this.legacyFormat = legacyFormat;
            this.legacyMessage = legacyMessage;
        }
    }

    /** Weak keys: nothing can leak if an event never reaches MONITOR. */
    private final Map<AsyncPlayerChatEvent, Pending> pending = Collections.synchronizedMap(new WeakHashMap<>());

    public void register(ChatEx plugin) {
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(Config.PRIORITY.getString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Unknown EventPriority '" + Config.PRIORITY.getString() + "', using LOWEST.");
            priority = EventPriority.LOWEST;
        }
        plugin.getServer().getPluginManager().registerEvent(AsyncPlayerChatEvent.class, this, priority,
                (listener, event) -> {
                    if (event instanceof AsyncPlayerChatEvent) {
                        handle((AsyncPlayerChatEvent) event);
                    }
                }, plugin, true);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void handle(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("chatex.allowchat")) {
            Map<String, String> values = new HashMap<>();
            values.put("%perm%", "chatex.allowchat");
            Locales.COMMAND_RESULT_NO_PERM.send(player, values);
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();

        if (!ChatRules.check(player, message, Config.ANTISPAM_ENABLED.getBoolean(), true, true)) {
            event.setCancelled(true);
            return;
        }

        boolean ranged = Config.RANGEMODE.getBoolean();
        String prefix = Config.RANGEPREFIX.getString();
        String rawFormat;

        if (ranged && !prefix.isEmpty() && message.startsWith(prefix)) {
            if (!player.hasPermission("chatex.chat.global")) {
                Map<String, String> values = new HashMap<>();
                values.put("%perm%", "chatex.chat.global");
                Locales.COMMAND_RESULT_NO_PERM.send(player, values);
                event.setCancelled(true);
                return;
            }
            message = message.substring(prefix.length()).stripLeading();
            if (message.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            rawFormat = PluginManager.getInstance().getGlobalMessageFormat(player);
        } else {
            rawFormat = PluginManager.getInstance().getMessageFormat(player);
            if (ranged) {
                List<Player> local = Utils.getLocalRecipients(player);
                if (local.size() <= 1 && Config.SHOW_NO_RECEIVER_MSG.getBoolean()) {
                    Locales.NO_LISTENING_PLAYERS.send(player);
                    if (!Config.NO_PLAYERS_KEEP_MESSAGE.getBoolean()) {
                        event.setCancelled(true);
                        return;
                    }
                }
                event.getRecipients().clear();
                event.getRecipients().addAll(local);
            }
        }

        // 1. permissions of the player + legacy / MiniMessage -> MiniMessage
        MessageGuard.Result checked = MessageGuard.process(message, player);
        String[] personal = ColorManager.wrap(player);
        if (personal != null && checked.usedStyle() && Config.PERSONAL_COLOR_BLOCKS_CODES.getBoolean()) {
            // a personal /color is set: no own colors or text modifiers in chat (hover, click, ... are still allowed by their permissions)
            Locales.PERSONAL_COLOR_ACTIVE.send(player);
            event.setCancelled(true);
            return;
        }
        if (!checked.denied().isEmpty()) {
            for (MessageGuard.Check check : checked.denied()) {
                check.message().send(player);
            }
            event.setCancelled(true);
            return;
        }
        String messageMM = checked.mm();
        if (personal != null) {
            messageMM = personal[0] + messageMM + personal[1];
        }

        // 2. placeholders -> text, legacy codes -> MiniMessage
        String formatMM = Fmt.toMM(Utils.replacePlayerPlaceholders(player, rawFormat));

        // 3. render
        Rendered formatRendered = MiniParser.parse(formatMM.replace("%message%", SENTINEL));
        Rendered messageRendered = MiniParser.parse(messageMM);
        String legacyFormat = formatRendered.toLegacy().replace("%", "%%").replace(SENTINEL, "%2$s");
        String legacyMessage = messageRendered.toLegacy();

        LogHelper.debug("Final format: " + legacyFormat);
        event.setFormat(legacyFormat);
        event.setMessage(legacyMessage);

        if (Config.SYSTEM_MESSAGES.getBoolean() || formatRendered.needsComponents() || messageRendered.needsComponents()) {
            pending.put(event, new Pending(formatMM, messageMM, legacyFormat, legacyMessage));
        }

        String plain = Fmt.plain(message);
        if (ranged) {
            MentionManager.processMentionsWithRecipients(player, plain, event.getRecipients());
        } else {
            MentionManager.processMentions(player, plain);
        }

        ChatLogger.writeToFile(player, message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMonitor(AsyncPlayerChatEvent event) {
        Pending p = pending.remove(event);
        if (p == null) return;

        Player player = event.getPlayer();
        String formatMM = p.formatMM;
        String messageMM = p.messageMM;

        // another plugin changed the format or the message after us: respect that
        if (!event.getFormat().equals(p.legacyFormat)) {
            String f = event.getFormat().replace("%%", "\u0001")
                    .replace("%1$s", player.getDisplayName())
                    .replace("%2$s", "%message%")
                    .replace("\u0001", "%");
            formatMM = Fmt.toMM(f);
        }
        if (!event.getMessage().equals(p.legacyMessage)) {
            messageMM = Fmt.toMM(event.getMessage().replace("\\", "\\\\").replace("<", "\\<"));
        }

        Rendered line = MiniParser.parse(formatMM.replace("%message%", messageMM));
        BaseComponent[] components = line.toComponents();

        for (Player recipient : event.getRecipients()) {
            recipient.spigot().sendMessage(components);
        }
        Bukkit.getConsoleSender().sendMessage(line.plain());
        event.setCancelled(true);
    }

    public void clear() {
        pending.clear();
    }
}
