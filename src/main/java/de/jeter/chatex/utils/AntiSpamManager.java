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

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AntiSpamManager {

    private static final AntiSpamManager INSTANCE = new AntiSpamManager();
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    private AntiSpamManager() {}

    public static AntiSpamManager getInstance() {
        return INSTANCE;
    }

    public boolean isAllowed(Player player) {
        if (!Config.ANTISPAM_ENABLED.getBoolean() || player.hasPermission("chatex.antispam.bypass")) {
            return true;
        }

        Long last = lastMessageTime.get(player.getUniqueId());
        if (last == null) {
            return true;
        }

        long cooldownMillis = Config.ANTISPAM_SECONDS.getInt() * 1000L;
        return System.currentTimeMillis() - last >= cooldownMillis;
    }

    public void update(Player player) {
        lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public long getRemainingSeconds(Player player) {
        Long last = lastMessageTime.get(player.getUniqueId());
        if (last == null) {
            return 0;
        }

        long cooldownMillis = Config.ANTISPAM_SECONDS.getInt() * 1000L;
        long elapsed = System.currentTimeMillis() - last;
        long remaining = cooldownMillis - elapsed;

        return remaining > 0 ? TimeUnit.MILLISECONDS.toSeconds(remaining) : 0;
    }

    public void remove(Player player) {
        lastMessageTime.remove(player.getUniqueId());
    }

    public void clear() {
        lastMessageTime.clear();
    }
}