package de.jeter.chatex.utils;

import de.jeter.chatex.ChatEx;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorManager {

    private static final Map<UUID, String> colors = new HashMap<>();
    private static final File file = new File(ChatEx.getInstance().getDataFolder(), "userdata.yml");
    private static YamlConfiguration cfg;

    public static void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        for (String nick : cfg.getKeys(false)) {
            String color = cfg.getString(nick + ".color");
            String uuidStr = cfg.getString(nick + ".uuid");
            if (color != null && uuidStr != null) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    colors.put(uuid, color);
                } catch (IllegalArgumentException e) {
                    ChatEx.getInstance().getLogger().warning("Invalid UUID in userdata.yml for " + nick + ": " + uuidStr);
                }
            }
        }
    }

    public static void save() {
        cfg = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : colors.entrySet()) {
            UUID uuid = entry.getKey();
            String color = entry.getValue();
            String nick = getNickByUUID(uuid);
            if (nick == null) {
                nick = uuid.toString();
            }
            cfg.set(nick + ".color", color);
            cfg.set(nick + ".uuid", uuid.toString());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getNickByUUID(UUID uuid) {
        Player player = ChatEx.getInstance().getServer().getPlayer(uuid);
        return player != null ? player.getName() : null;
    }

    public static String getPersonalColor(Player player) {
        String raw = colors.getOrDefault(player.getUniqueId(), "");
        if (raw.isEmpty()) return "";
    
        String processed = raw.replace("&#", "#");
    
        Pattern hexPattern = Pattern.compile("#[0-9a-fA-F]{6}");
        Matcher matcher = hexPattern.matcher(processed);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group().substring(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
    
        return sb.toString().replace("&", "§");
    }

    public static void setPersonalColor(Player player, String rawColor) {
        String processed = Utils.translateColorCodes(rawColor, player);
        if (processed != null) {
            colors.put(player.getUniqueId(), rawColor);
            save();
        }
    }

    public static void removePersonalColor(Player player) {
        colors.remove(player.getUniqueId());
        save();
    }
}