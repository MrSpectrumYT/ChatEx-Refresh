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
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private static Map<String, String> cmd(Command command) {
        Map<String, String> values = new HashMap<>();
        values.put("%cmd%", command.getName());
        return values;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            if (args.length > 1) {
                Locales.COMMAND_RESULT_WRONG_USAGE.send(sender, cmd(command));
            } else {
                Locales.HELP_MESSAGE.send(sender, cmd(command));
            }
            return true;
        }

        if (args.length > 1) {
            Locales.COMMAND_RESULT_WRONG_USAGE.send(sender, cmd(command));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("chatex.reload")) {
                Map<String, String> values = new HashMap<>();
                values.put("%perm%", "chatex.reload");
                Locales.COMMAND_RESULT_NO_PERM.send(sender, values);
                return true;
            }
            if (ChatEx.getInstance().reloadAll()) {
                Locales.MESSAGES_RELOAD.send(sender);
            } else {
                Integrity.reportFailures(sender);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (!sender.hasPermission("chatex.clear")) {
                Map<String, String> values = new HashMap<>();
                values.put("%perm%", "chatex.clear");
                Locales.COMMAND_RESULT_NO_PERM.send(sender, values);
                return true;
            }

            String clearerName;
            if (sender instanceof Player) {
                clearerName = sender.getName();
            } else if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
                clearerName = Locales.COMMAND_CLEAR_CONSOLE.getPlain();
            } else {
                clearerName = Locales.COMMAND_CLEAR_UNKNOWN.getPlain();
            }
            Map<String, String> values = new HashMap<>();
            values.put("%clearer%", clearerName);

            // one message with many empty lines pushes the old chat out of the screen
            String blank = "§r";
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 150; i++) {
                    player.sendMessage(blank);
                }
                Locales.MESSAGES_CLEAR.send(player, values);
            }
            Locales.MESSAGES_CLEAR.send(Bukkit.getConsoleSender(), values);
            return true;
        }

        Map<String, String> values = new HashMap<>();
        values.put("%cmd%", "/chatex");
        Locales.COMMAND_RESULT_WRONG_USAGE.send(sender, values);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabs = new ArrayList<>();
        if (args.length > 1) return tabs;
        tabs.add("help");
        if (sender.hasPermission("chatex.clear")) tabs.add("clear");
        if (sender.hasPermission("chatex.reload")) tabs.add("reload");
        if (args.length == 1) {
            String start = args[0].toLowerCase();
            tabs.removeIf(s -> !s.startsWith(start));
        }
        return tabs;
    }
}
