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

import de.jeter.chatex.ChatEx;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MentionManager {

    private static volatile String cachedName;
    private static volatile Sound cachedSound;
    private static volatile String cachedKey;

    public static void processMentions(Player sender, String message) {
        processMentionsWithRecipients(sender, message, null);
    }

    /** {@code message} must be plain text (no color codes). */
    public static void processMentionsWithRecipients(Player sender, String message, Collection<Player> recipients) {
        if (!Config.MENTION_ENABLED.getBoolean()) return;
        if (!sender.hasPermission("chatex.mention")) return;

        Set<Player> mentioned = new HashSet<>();
        for (String word : message.split("\\s+")) {
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
            ChatEx.getInstance().getTaskManager().runForEntity(player, () -> playMentionSound(player));
        }
    }

    /**
     * The sound is looked up by reflection, because Sound is an enum on old servers and an
     * interface on new ones. The config accepts SOUND_NAME or a key like block.note_block.pling.
     */
    private static void resolve(String name) {
        if (name.equals(cachedName)) return;
        Sound found = null;
        String key = null;
        try {
            Object o = Sound.class.getField(name.toUpperCase(Locale.ROOT).replace('.', '_')).get(null);
            if (o instanceof Sound) found = (Sound) o;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            key = name.toLowerCase(Locale.ROOT);
        }
        if (found == null && key == null) key = "block.note_block.pling";
        cachedSound = found;
        cachedKey = key;
        cachedName = name;
    }

    private static void playMentionSound(Player player) {
        resolve(Config.MENTION_SOUND.getString());
        Sound sound = cachedSound;
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), cachedKey, 1.0f, 1.0f);
        }
    }
}
