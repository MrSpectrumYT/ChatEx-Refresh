package de.jeter.chatex.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ColorCommand implements CommandExecutor, TabCompleter {

    private static final List<String> LEGACY_COLORS = Arrays.asList(
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
            "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f"
    );

    private static final List<String> LEGACY_FORMATS = Arrays.asList(
            "&l", "&m", "&n", "&o", "&r"
    );

    private static final String MAGIC_FORMAT = "&k";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locales.COLOR_ONLY_PLAYERS.getString(null));
            return true;
        }

        if (!player.hasPermission("chatex.color")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%perm%", "chatex.color");
            player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(player, placeholders));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Locales.COLOR_USAGE.getString(player));
            return true;
        }

        String input = String.join(" ", args).trim();

        if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("reset")) {
            ColorManager.removePersonalColor(player);
            player.sendMessage(Locales.COLOR_RESET.getString(player));
            return true;
        }

        if (!isValidColorInput(input, player)) {
            player.sendMessage(Locales.COLOR_INVALID.getString(player));
            return true;
        }

        ColorManager.setPersonalColor(player, input);
        player.sendMessage(Locales.COLOR_SET_SUCCESS.getString(player));
        return true;
    }

    private boolean isValidColorInput(String input, Player player) {
        if (input.isEmpty()) return false;

        if (input.contains(",")) {
            if (!player.hasPermission("chatex.chat.colorgradient")) {
                return false;
            }

            String[] parts = input.split(",");
            int colorCount = 0;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                
                if (i == parts.length - 1) {
                    int ampIndex = part.lastIndexOf('&');
                    if (ampIndex > 0) {
                        String mods = part.substring(ampIndex);
                        if (mods.matches("^(&[lmnork])+$")) {
                            if (mods.contains("&k") && !player.hasPermission("chatex.chat.magic")) return false;
                            if ((mods.contains("&l") || mods.contains("&m") || mods.contains("&n") || mods.contains("&o") || mods.contains("&r")) 
                                    && !player.hasPermission("chatex.chat.colormodifier")) return false;
                            part = part.substring(0, ampIndex);
                        } else {
                            return false;
                        }
                    }
                }

                if (part.matches("^(#|&#)[a-fA-F0-9]{6}$")) {
                    colorCount++;
                } else {
                    return false;
                }
            }

            return colorCount >= 2;
        }

        if (input.matches("^(#|&#)[a-fA-F0-9]{6}(&[lmnork])*$")) {
            if (!player.hasPermission("chatex.chat.colorhex")) {
                return false;
            }
            if (input.contains("&k") && !player.hasPermission("chatex.chat.magic")) {
                return false;
            }
            if (input.matches(".*&[l-or].*") && !player.hasPermission("chatex.chat.colormodifier")) {
                return false;
            }
            return true;
        }

        String[] parts = input.split("(?=&)");
        if (parts.length == 0) return false;

        int colorCount = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            String lowerPart = part.toLowerCase();

            if (lowerPart.equals(MAGIC_FORMAT)) {
                if (!player.hasPermission("chatex.chat.magic")) {
                    return false;
                }
                continue;
            }

            if (LEGACY_COLORS.contains(lowerPart)) {
                if (!player.hasPermission("chatex.chat.colorlegacy")) {
                    return false;
                }
                colorCount++;
                if (colorCount > 1) {
                    return false;
                }
                continue;
            }

            if (LEGACY_FORMATS.contains(lowerPart)) {
                if (!player.hasPermission("chatex.chat.colormodifier")) {
                    return false;
                }
                continue;
            }

            return false;
        }

        return colorCount > 0 || input.contains("&");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return suggestions;
        }

        if (!player.hasPermission("chatex.color")) {
            return suggestions;
        }

        String currentInput = String.join(" ", args);

        if (args.length == 0 || currentInput.isEmpty()) {
            addAvailableOptions(suggestions, player);
            return suggestions;
        }

        currentInput = currentInput.trim();
        String lowerInput = currentInput.toLowerCase();

        if ((lowerInput.startsWith("#") || lowerInput.startsWith("&#")) && lowerInput.contains(",")) {
            suggestions.add(currentInput);
            return suggestions;
        }

        if (player.hasPermission("chatex.chat.colorhex")) {
            if (lowerInput.startsWith("#") || lowerInput.startsWith("&#")) {
                String hexPart = lowerInput.replace("&#", "#");
                if (hexPart.matches("^#[a-fA-F0-9]{0,6}$")) {
                    suggestions.add(currentInput);
                }
                if (hexPart.matches("^#[a-fA-F0-9]{6}$")) {
                    if (player.hasPermission("chatex.chat.colorgradient")) {
                        suggestions.add(currentInput + ",");
                    }
                    if (player.hasPermission("chatex.chat.colormodifier")) {
                        for (String f : LEGACY_FORMATS) {
                            suggestions.add(currentInput + f);
                        }
                    }
                    if (player.hasPermission("chatex.chat.magic")) {
                        suggestions.add(currentInput + MAGIC_FORMAT);
                    }
                }
                return suggestions;
            }
        }

        if (currentInput.contains("&")) {
            int lastAmpersand = currentInput.lastIndexOf('&');
            String before = currentInput.substring(0, lastAmpersand);
            String after = currentInput.substring(lastAmpersand + 1);

            int colorCount = 0;
            boolean hasMagic = false;
            for (String color : LEGACY_COLORS) {
                if (currentInput.toLowerCase().contains(color.toLowerCase())) {
                    colorCount++;
                }
            }
            if (currentInput.toLowerCase().contains(MAGIC_FORMAT)) {
                hasMagic = true;
            }

            boolean hasHex = currentInput.matches("^(#|&#)[a-fA-F0-9]{6}.*");

            if (player.hasPermission("chatex.chat.colorlegacy") && colorCount == 0 && !hasHex) {
                for (String c : LEGACY_COLORS) {
                    if (c.substring(1).toLowerCase().startsWith(after.toLowerCase())) {
                        suggestions.add(before + c);
                    }
                }
            }

            if (player.hasPermission("chatex.chat.magic") && !hasMagic) {
                if (MAGIC_FORMAT.substring(1).toLowerCase().startsWith(after.toLowerCase())) {
                    suggestions.add(before + MAGIC_FORMAT);
                }
            }

            if (player.hasPermission("chatex.chat.colormodifier")) {
                for (String f : LEGACY_FORMATS) {
                    if (f.substring(1).toLowerCase().startsWith(after.toLowerCase())) {
                        suggestions.add(before + f);
                    }
                }
            }
            return suggestions;
        }

        addAvailableOptions(suggestions, player);
        return suggestions;
    }

    private void addAvailableOptions(List<String> suggestions, Player player) {
        if (player.hasPermission("chatex.chat.colorlegacy")) {
            suggestions.addAll(LEGACY_COLORS);
        }
        if (player.hasPermission("chatex.chat.colormodifier")) {
            suggestions.addAll(LEGACY_FORMATS);
        }
        if (player.hasPermission("chatex.chat.colorhex")) {
            suggestions.add("#");
            suggestions.add("&#");
        }
        if (player.hasPermission("chatex.chat.colorgradient")) {
            suggestions.add("#FF0000,#00FF00");
            suggestions.add("&#FF0000,#00FF00");
        }
        if (player.hasPermission("chatex.chat.magic")) {
            suggestions.add(MAGIC_FORMAT);
        }
        suggestions.add("off");
        suggestions.add("reset");
    }
}