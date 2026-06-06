package de.jeter.chatex;

import de.jeter.chatex.utils.ColorManager;
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
            String help = Locales.HELP_MESSAGE.getString(null)
                    .replace("%cmd%", command.getName());
            sender.sendMessage(help);
            return true;
        }
        
        if (args.length > 1) {
            String wrongUsage = Locales.COMMAND_RESULT_WRONG_USAGE.getString(null)
                    .replace("%cmd%", command.getName());
            sender.sendMessage(wrongUsage);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chatex.reload")) {
                Config.reload(true);
                Locales.fullReload();
                ColorManager.load();
                sender.sendMessage(Locales.MESSAGES_RELOAD.getString(null));

                if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        String name = Config.TABLIST_FORMAT.getString();
                        name = Utils.replacePlayerPlaceholders(p, name);
                        p.setPlayerListName(name);
                    }
                }
            } else {
                String noPerm = Locales.COMMAND_RESULT_NO_PERM.getString(null)
                        .replace("%perm%", "chatex.reload");
                sender.sendMessage(noPerm);
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
        
                String message = Locales.MESSAGES_CLEAR.getString(null)
                        .replace("%clearer%", clearerName);
        
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                }
                Bukkit.getConsoleSender().sendMessage(message);
            } else {
                String noPerm = Locales.COMMAND_RESULT_NO_PERM.getString(null)
                        .replace("%perm%", "chatex.clear");
                sender.sendMessage(noPerm);
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            String help = Locales.HELP_MESSAGE.getString(null)
                    .replace("%cmd%", command.getName());
            sender.sendMessage(help);
            return true;
        }
        
        String wrongUsage = Locales.COMMAND_RESULT_WRONG_USAGE.getString(null)
                .replace("%cmd%", "/chatex");
        sender.sendMessage(wrongUsage);
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