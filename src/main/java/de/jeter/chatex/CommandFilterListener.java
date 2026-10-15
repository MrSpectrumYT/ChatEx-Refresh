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

import de.jeter.chatex.text.MessageGuard;
import de.jeter.chatex.text.MiniParser;
import de.jeter.chatex.utils.ChatRules;
import de.jeter.chatex.utils.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Applies the ChatEx rules (colors, ads, spam, blocked words) to the commands of Commands.List. */
public class CommandFilterListener implements Listener {

    private static volatile Map<String, Integer> commands = new HashMap<>();

    /** Reads Commands.List. Call after the config was (re)loaded. */
    public static void rebuild() {
        Map<String, Integer> map = new HashMap<>();
        for (String entry : Config.COMMANDS_LIST.getStringList()) {
            String[] parts = entry.trim().split("\\s+");
            String name = parts[0].toLowerCase(Locale.ROOT);
            if (name.startsWith("/")) name = name.substring(1);
            if (name.isEmpty()) continue;
            int skip = 0;
            if (parts.length > 1) {
                try {
                    skip = Math.max(0, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                    // keep 0
                }
            }
            map.put(name, skip);
        }
        commands = map;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!Config.COMMANDS_ENABLED.getBoolean()) return;

        String full = event.getMessage();
        int space = full.indexOf(' ');
        if (full.length() < 2 || space < 0) return;

        String label = full.substring(1, space).toLowerCase(Locale.ROOT);
        int colon = label.indexOf(':');
        if (colon >= 0) label = label.substring(colon + 1);

        Integer skip = commands.get(label);
        if (skip == null) return;

        int pos = space;
        for (int k = 0; k < skip; k++) {
            while (pos < full.length() && full.charAt(pos) == ' ') pos++;
            while (pos < full.length() && full.charAt(pos) != ' ') pos++;
        }
        while (pos < full.length() && full.charAt(pos) == ' ') pos++;
        if (pos >= full.length()) return;

        String head = full.substring(0, pos);
        String text = full.substring(pos);
        Player player = event.getPlayer();

        if (!ChatRules.check(player, text, Config.COMMANDS_SPAM.getBoolean() && Config.ANTISPAM_ENABLED.getBoolean(),
                Config.COMMANDS_ADS.getBoolean(), Config.COMMANDS_WORDS.getBoolean())) {
            event.setCancelled(true);
            return;
        }

        if (Config.COMMANDS_COLORS.getBoolean()) {
            MessageGuard.Result checked = MessageGuard.process(text, player);
            if (!checked.denied().isEmpty()) {
                for (MessageGuard.Check check : checked.denied()) {
                    check.message().send(player);
                }
                event.setCancelled(true);
                return;
            }
            if (checked.changed()) {
                event.setMessage(head + MiniParser.parse(checked.mm()).toLegacy());
            }
        }
    }
}
