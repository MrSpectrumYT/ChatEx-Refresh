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

import de.jeter.chatex.text.Fmt;
import de.jeter.chatex.utils.adManager.AdManager;
import de.jeter.chatex.utils.adManager.SimpleAdManager;
import de.jeter.chatex.utils.adManager.SmartAdManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** The anti-spam / ad blocker / blocked words rules. Used by the chat and by the filtered commands. */
public final class ChatRules {

    private static final AdManager SIMPLE = new SimpleAdManager();
    private static final AdManager SMART = new SmartAdManager();

    private ChatRules() {
    }

    public static AdManager adManager() {
        return Config.ADS_SMART_MANAGER.getBoolean() ? SMART : SIMPLE;
    }

    /**
     * @return true if the text is allowed. Otherwise the player already got the message.
     */
    public static boolean check(Player player, String message, boolean spam, boolean ads, boolean words) {
        if (spam) {
            AntiSpamManager spamManager = AntiSpamManager.getInstance();
            if (!spamManager.isAllowed(player)) {
                Map<String, String> values = new HashMap<>();
                values.put("%time%", String.valueOf(spamManager.getRemainingSeconds(player)));
                Locales.ANTI_SPAM_DENIED.send(player, values);
                return false;
            }
            spamManager.update(player);
        }

        // Everything the player typed is checked: the visible text (colors must not hide an ad,
        // "exa&cmple.com") AND the raw string, which also contains the values of tags like
        // <click:open_url:...> and <hover:show_text:'...'>.
        String plain = Fmt.plain(message);

        if (ads) {
            AdManager manager = adManager();
            boolean found = manager.checkForAds(plain, player);
            if (!found && !plain.equals(message)) {
                found = manager.checkForAds(message, player);
            }
            if (found) {
                Locales.MESSAGES_AD.send(player);
                return false;
            }
        }

        if (words) {
            String lower = plain.toLowerCase(Locale.ROOT);
            String lowerRaw = message.toLowerCase(Locale.ROOT);
            for (String blocked : Config.BLOCKED_WORDS.getStringList()) {
                String b = blocked.toLowerCase(Locale.ROOT);
                if (!b.isEmpty() && (lower.contains(b) || lowerRaw.contains(b))) {
                    Locales.MESSAGES_BLOCKED.send(player);
                    return false;
                }
            }
        }
        return true;
    }
}
