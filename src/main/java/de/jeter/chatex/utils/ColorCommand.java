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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorCommand implements CommandExecutor, TabCompleter {

    private static final List<String> LEGACY_COLORS = Arrays.asList(
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
            "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f"
    );

    private static final List<String> LEGACY_FORMATS = Arrays.asList(
            "&l", "&m", "&n", "&o"
        );

    private static final String MAGIC_FORMAT = "&k";

    private static final String PERM_BASE = "chatex.color";
    private static final String PERM_LEGACY = "chatex.chat.colorlegacy";
    private static final String PERM_MODIFIER = "chatex.chat.colormodifier";
    private static final String PERM_MAGIC = "chatex.chat.magic";
    private static final String PERM_HEX = "chatex.chat.colorhex";
    private static final String PERM_GRADIENT = "chatex.chat.colorgradient";

    private static final Pattern HEX_PATTERN = Pattern.compile("^(&?#)([0-9a-fA-F]*)(.*)$");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("^(&[0-9a-fA-F])(.*)$");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locales.COLOR_ONLY_PLAYERS.getString(null));
            return true;
        }

        if (!player.hasPermission(PERM_BASE)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%perm%", PERM_BASE);
            player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getString(player, placeholders));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Locales.COLOR_USAGE.getString(player));
            return true;
        }

        String input = String.join(" ", args).trim();

        if (input.equalsIgnoreCase("reset")) {
            ColorManager.removePersonalColor(player);
            player.sendMessage(Locales.COLOR_RESET.getString(player));
            return true;
        }

        String errorMessage = validateColorInput(input, player);
        if (errorMessage != null) {
            player.sendMessage(errorMessage);
            return true;
        }

        ColorManager.setPersonalColor(player, input);
        player.sendMessage(Locales.COLOR_SET_SUCCESS.getString(player));
        return true;
    }

    private String validateColorInput(String input, Player player) {
        if (input.isEmpty()) {
            return Locales.COLOR_INVALID.getString(player);
        }

        if (input.contains(",")) {
            if (!player.hasPermission(PERM_GRADIENT)) {
                return Locales.NO_GRADIENT_PERMISSION.getString(player);
            }

            String[] parts = input.split(",");
            if (parts.length < 2) {
                return Locales.COLOR_GRADIENT_MIN_TWO.getString(player);
            }

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                
                if (i == parts.length - 1) {
                    int ampIndex = part.lastIndexOf('&');
                    if (ampIndex > 0) {
                        String mods = part.substring(ampIndex);
                        if (mods.matches("^(&[lmnok])+$")) {
                            if (mods.contains("&k") && !player.hasPermission(PERM_MAGIC)) {
                                return Locales.MAGIC_BLOCKED.getString(player);
                            }
                            if ((mods.contains("&l") || mods.contains("&m") || mods.contains("&n") || mods.contains("&o")) && 
                                !player.hasPermission(PERM_MODIFIER)) {
                                return Locales.NO_MODIFIER_PERMISSION.getString(player);
                            }
                            part = part.substring(0, ampIndex);
                        } else {
                            return Locales.COLOR_INVALID.getString(player);
                        }
                    }
                }

                if (!part.matches("^(#|&#)[a-fA-F0-9]{6}$")) {
                    return Locales.COLOR_INVALID.getString(player);
                }
            }
            return null;
        }

        if (input.matches("^(#|&#)[a-fA-F0-9]{6}(&[lmnok])*$")) {
            if (!player.hasPermission(PERM_HEX)) {
                return Locales.NO_HEX_PERMISSION.getString(player);
            }
            if (input.contains("&k") && !player.hasPermission(PERM_MAGIC)) {
                return Locales.MAGIC_BLOCKED.getString(player);
            }
            if (input.matches(".*&[l-o].*") && !player.hasPermission(PERM_MODIFIER)) {
                return Locales.NO_MODIFIER_PERMISSION.getString(player);
            }
            return null;
        }

        String[] parts = input.split("(?=&)");
        if (parts.length == 0) {
            return Locales.COLOR_INVALID.getString(player);
        }

        int colorCount = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            String lowerPart = part.toLowerCase();

            if (lowerPart.equals(MAGIC_FORMAT)) {
                if (!player.hasPermission(PERM_MAGIC)) {
                    return Locales.MAGIC_BLOCKED.getString(player);
                }
                continue;
            }

            if (LEGACY_COLORS.contains(lowerPart)) {
                if (!player.hasPermission(PERM_LEGACY)) {
                    return Locales.NO_COLOR_PERMISSION.getString(player);
                }
                colorCount++;
                if (colorCount > 1) {
                    return Locales.COLOR_INVALID.getString(player);
                }
                continue;
            }

            if (LEGACY_FORMATS.contains(lowerPart)) {
                if (!player.hasPermission(PERM_MODIFIER)) {
                    return Locales.NO_MODIFIER_PERMISSION.getString(player);
                }
                continue;
            }

            return Locales.COLOR_INVALID.getString(player);
        }

        if (colorCount == 0 && !input.contains("&")) {
            return Locales.COLOR_INVALID.getString(player);
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player) || !player.hasPermission(PERM_BASE) || args.length > 1) {
            return suggestions;
        }

        String input = args.length == 0 ? "" : args[0];

        List<String> allowedModifiers = new ArrayList<>();
        if (player.hasPermission(PERM_MODIFIER)) {
            allowedModifiers.addAll(LEGACY_FORMATS);
        }
        if (player.hasPermission(PERM_MAGIC)) {
            allowedModifiers.add(MAGIC_FORMAT);
        }

        if (input.isEmpty()) {
            if (player.hasPermission(PERM_LEGACY)) suggestions.addAll(LEGACY_COLORS);
            suggestions.addAll(allowedModifiers);
            if (player.hasPermission(PERM_HEX)) {
                suggestions.add("#");
                suggestions.add("&#");
            }
            suggestions.add("reset");
            return suggestions;
        }

        if (input.contains(",")) {
            int lastCommaIndex = input.lastIndexOf(',');
            String prefix = input.substring(0, lastCommaIndex + 1);
            String lastPart = input.substring(lastCommaIndex + 1);

            if (lastPart.isEmpty()) {
                if (player.hasPermission(PERM_HEX)) {
                    suggestions.add(prefix + "#");
                    suggestions.add(prefix + "&#");
                }
                return suggestions;
            }

            Matcher hexMatcher = HEX_PATTERN.matcher(lastPart);
            if (hexMatcher.matches()) {
                String hexBody = hexMatcher.group(2);
                String currentMods = hexMatcher.group(3);

                if (hexBody.length() < 6) {
                    return filterByPrefix(suggestions, input);
                } else if (hexBody.length() == 6) {
                    if (currentMods.isEmpty()) {
                        if (player.hasPermission(PERM_GRADIENT)) {
                            suggestions.add(input + ",");
                        }
                        for (String mod : allowedModifiers) {
                            suggestions.add(input + mod);
                        }
                    } else {
                        handleModifiersCompletion(input, currentMods, allowedModifiers, suggestions);
                    }
                }
            }
            return filterByPrefix(suggestions, input);
        }

        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        if (hexMatcher.matches()) {
            String hexBody = hexMatcher.group(2);
            String currentMods = hexMatcher.group(3);

            if (hexBody.length() < 6) {
                return filterByPrefix(suggestions, input); 
            } else if (hexBody.length() == 6) {
                if (currentMods.isEmpty()) {
                    if (player.hasPermission(PERM_GRADIENT)) {
                        suggestions.add(input + ",");
                    }
                    for (String mod : allowedModifiers) {
                        suggestions.add(input + mod);
                    }
                } else {
                    handleModifiersCompletion(input, currentMods, allowedModifiers, suggestions);
                }
            }
            return filterByPrefix(suggestions, input);
        }

        Matcher legacyMatcher = LEGACY_PATTERN.matcher(input);
        if (legacyMatcher.matches()) {
            String currentMods = legacyMatcher.group(2);
            handleModifiersCompletion(input, currentMods, allowedModifiers, suggestions);
            return filterByPrefix(suggestions, input);
        }

        suggestions.add("reset");
        if (player.hasPermission(PERM_LEGACY)) suggestions.addAll(LEGACY_COLORS);
        suggestions.addAll(allowedModifiers);
        if (player.hasPermission(PERM_HEX)) {
            suggestions.add("#");
            suggestions.add("&#");
        }

        return filterByPrefix(suggestions, input);
    }

    private void handleModifiersCompletion(String fullInput, String currentMods, List<String> allowedModifiers, List<String> suggestions) {
        if (fullInput.endsWith("&")) {
            for (String mod : allowedModifiers) {
                if (!currentMods.contains(mod)) {
                    suggestions.add(fullInput + mod.substring(1));
                }
            }
            return;
        }

        for (String mod : allowedModifiers) {
            if (!currentMods.contains(mod)) {
                suggestions.add(fullInput + mod);
            }
        }
    }

    private List<String> filterByPrefix(List<String> list, String prefix) {
        List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}