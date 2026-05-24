/*
 * This file is part of ChatEx Refresh
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This file is part of ChatEx
 * Copyright (C) 2022 ChatEx Team
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

import de.jeter.chatex.utils.Config;
import de.jeter.chatex.utils.Locales;
import de.jeter.chatex.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aChatEx-Refresh plugin by " + ChatEx.getInstance().getDescription().getAuthors());
            return true;
        }
        
        if (args.length > 1) {
            sender.sendMessage(Locales.COMMAND_RESULT_WRONG_USAGE.getString(null).replaceAll("%cmd", command.getName()));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chatex.reload")) {
                Config.reload(true);
                Locales.fullReload();
                sender.sendMessage(Locales.MESSAGES_RELOAD.getString(null));

                if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        String name = Config.TABLIST_FORMAT.getString();
                        name = Utils.replacePlayerPlaceholders(p, name);
                        p.setPlayerListName(name);
                    }
                }
            } else {
                sender.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(null).replaceAll("%perm", "chatex.reload"));
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("clear")) {
            if (sender.hasPermission("chatex.clear")) {
                clearChat();
        
                String clearerName;
                if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
                    clearerName = Locales.COMMAND_CLEAR_CONSOLE.getString(null);
                } else if (sender instanceof Player) {
                    clearerName = sender.getName();
                } else {
                    clearerName = Locales.COMMAND_CLEAR_UNKNOWN.getString(null);
                }
        
                String message = Locales.MESSAGES_CLEAR.getString(null);
                message = message.replace("%clearer%", clearerName);
        
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                }
                Bukkit.getConsoleSender().sendMessage(message);
            } else {
                sender.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(null).replaceAll("%perm", "chatex.clear"));
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            sender.sendMessage("§f ");
            sender.sendMessage("§e☀ §fHelp for chat:");
            sender.sendMessage("§f● §e/" + command.getName() + " clear §f— " + Locales.COMMAND_CLEAR_DESCRIPTION.getString(null));
            sender.sendMessage("§f● §e/" + command.getName() + " reload §f— " + Locales.COMMAND_RELOAD_DESCRIPTION.getString(null));
            sender.sendMessage("§f ");
            return true;
        }
        
        sender.sendMessage(Locales.COMMAND_RESULT_WRONG_USAGE.getString(null).replaceAll("%cmd", "/chatex"));
        return true;
    }

    private void clearChat() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 200; i++) {
                player.sendMessage("");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> possibleTabs = new ArrayList<>();
        if (commandSender.hasPermission("chatex.clear")) {
            possibleTabs.add("clear");
        }
        if (commandSender.hasPermission("chatex.reload")) {
            possibleTabs.add("reload");
        }
        return possibleTabs;
    }
}