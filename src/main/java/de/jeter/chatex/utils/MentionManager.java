package de.jeter.chatex.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MentionManager {

    public static void processMentions(Player sender, String message) {
        if (!Config.MENTION_ENABLED.getBoolean()) return;
        if (!sender.hasPermission("chatex.mention")) return;

        String[] words = message.split("\\s+");
        Set<Player> mentioned = new HashSet<>();

        for (String word : words) {
            String cleanWord = word.replaceAll("[^\\p{L}\\p{N}_]", "");
            
            if (cleanWord.isEmpty() || cleanWord.equalsIgnoreCase(sender.getName())) {
                continue;
            }

            Player target = Bukkit.getPlayerExact(cleanWord);
            if (target != null && target.isOnline()) {
                mentioned.add(target);
            }
        }

        for (Player player : mentioned) {
            playMentionSound(player);
        }
    }

    private static void playMentionSound(Player player) {
        try {
            Sound sound = Sound.valueOf(Config.MENTION_SOUND.getString().toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }
}