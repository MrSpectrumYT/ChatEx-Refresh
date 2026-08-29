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
package de.jeter.chatex.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MentionManager {

    public static void processMentions(Player sender, String message) {
        processMentionsWithRecipients(sender, message, null);
    }

    public static void processMentionsWithRecipients(Player sender, String message, Collection<Player> recipients) {
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
                if (recipients != null && !recipients.contains(target)) {
                    continue;
                }
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