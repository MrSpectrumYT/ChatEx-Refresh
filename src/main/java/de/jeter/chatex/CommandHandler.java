package de.jeter.chatex;

import de.jeter.chatex.utils.Config;
import de.jeter.chatex.utils.Locales;
import de.jeter.chatex.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("ChatEx-Refresh plugin by " + ChatEx.getInstance().getDescription().getAuthors(), NamedTextColor.GREEN));
            return true;
        }
        
        if (args.length > 1) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%cmd", command.getName());
            sender.sendMessage(Locales.COMMAND_RESULT_WRONG_USAGE.getComponent(null, placeholders));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chatex.reload")) {
                Config.reload(true);
                Locales.fullReload();
                sender.sendMessage(Locales.MESSAGES_RELOAD.getComponent(null));

                if (Config.CHANGE_TABLIST_NAME.getBoolean()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        String name = Config.TABLIST_FORMAT.getString();
                        name = Utils.replacePlayerPlaceholders(p, name);
                        p.setPlayerListName(name);
                    }
                }
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%perm", "chatex.reload");
                sender.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getComponent(null, placeholders));
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("clear")) {
            if (sender.hasPermission("chatex.clear")) {
                clearChat();
        
                String clearerName;
                if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
                    clearerName = Locales.COMMAND_CLEAR_CONSOLE.getStringRaw();
                } else if (sender instanceof Player) {
                    clearerName = sender.getName();
                } else {
                    clearerName = Locales.COMMAND_CLEAR_UNKNOWN.getStringRaw();
                }
        
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%clearer%", clearerName);
                Component message = Locales.MESSAGES_CLEAR.getComponent(null, placeholders);
        
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                }
                Bukkit.getConsoleSender().sendMessage(message);
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%perm", "chatex.clear");
                sender.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getComponent(null, placeholders));
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("☀ Help for chat:", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("● /" + command.getName() + " clear — ", NamedTextColor.WHITE)
                    .append(Locales.COMMAND_CLEAR_DESCRIPTION.getComponent(null)));
            sender.sendMessage(Component.text("● /" + command.getName() + " reload — ", NamedTextColor.WHITE)
                    .append(Locales.COMMAND_RELOAD_DESCRIPTION.getComponent(null)));
            sender.sendMessage(Component.empty());
            return true;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%cmd", "/chatex");
        sender.sendMessage(Locales.COMMAND_RESULT_WRONG_USAGE.getComponent(null, placeholders));
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