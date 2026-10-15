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
package de.jeter.chatex.text;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry point of the text engine.
 * <p>
 * Order of work (used for locales, the 3 format lines of the config and chat messages):
 * <ol>
 *     <li>placeholders are replaced by plain text (with whatever colors they contain)</li>
 *     <li>every legacy color code (&amp;c, &sect;c, &amp;x&amp;f&amp;f..., &amp;#RRGGBB, #RRGGBB) is converted to MiniMessage</li>
 *     <li>the MiniMessage text is parsed and rendered (legacy string for Spigot formats, components for click/hover)</li>
 * </ol>
 */
public final class Fmt {

    private static final char SECTION = '\u00A7';

    /** Legacy color codes also reset all decorations, so a converted color tag does the same. */
    public static final String RESET_DECO = "<!b><!i><!u><!st><!obf>";

    /** &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B (BungeeCord hex format), also with the section sign. */
    public static final Pattern BUNGEE_HEX = Pattern.compile("[&\u00A7][xX](?:[&\u00A7][0-9a-fA-F]){6}");
    /** &amp;#RRGGBB */
    public static final Pattern AMP_HEX = Pattern.compile("[&\u00A7]#([0-9a-fA-F]{6})");
    /** #RRGGBB */
    public static final Pattern BARE_HEX = Pattern.compile("#([0-9a-fA-F]{6})");
    /** &amp;0-f, &amp;k-o, &amp;r (upper and lower case), also with the section sign. */
    public static final Pattern CODE = Pattern.compile("[&\u00A7]([0-9a-fA-FklmnorKLMNOR])");

    private Fmt() {
    }

    /** MiniMessage for a legacy format code (0-9a-fk-or). Colors reset decorations like in legacy. */
    public static String codeToMM(char code) {
        char c = Character.toLowerCase(code);
        int idx = Character.digit(c, 16);
        if (idx >= 0) return RESET_DECO + "<" + MiniParser.LEGACY_NAMES[idx] + ">";
        switch (c) {
            case 'k':
                return "<obfuscated>";
            case 'l':
                return "<bold>";
            case 'm':
                return "<strikethrough>";
            case 'n':
                return "<underlined>";
            case 'o':
                return "<italic>";
            case 'r':
                return "<reset>";
            default:
                return "";
        }
    }

    public static String hexToMM(String hex6) {
        return RESET_DECO + "<#" + hex6.toUpperCase() + ">";
    }

    /** Converts "&amp;x&amp;f&amp;f&amp;0&amp;0&amp;0&amp;0" to "FF0000". */
    public static String bungeeHexDigits(String match) {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 3; i < match.length(); i += 2) sb.append(match.charAt(i));
        return sb.toString();
    }

    /**
     * Converts every legacy code in a trusted text (config, locale, placeholders) to MiniMessage.
     * Existing MiniMessage tags are kept. A lone "&amp;x" that is not followed by 6 hex pairs stays literal.
     * A backslash escapes: \&lt; \\ (MiniMessage) and \&amp; \&sect; \# (kept as plain characters).
     */
    public static String toMM(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        int n = s.length();
        StringBuilder out = new StringBuilder(n + 16);
        Matcher bungee = BUNGEE_HEX.matcher(s);
        Matcher ampHex = AMP_HEX.matcher(s);
        Matcher bare = BARE_HEX.matcher(s);
        Matcher code = CODE.matcher(s);

        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < n) {
                char next = s.charAt(i + 1);
                if (next == '<' || next == '\\') {
                    out.append(c).append(next);
                    i += 2;
                    continue;
                }
                if (next == '&' || next == SECTION || next == '#') {
                    out.append(next);
                    i += 2;
                    continue;
                }
            } else if (c == '<') {
                MiniParser.Tag tag = MiniParser.scanTag(s, i);
                if (tag != null) {
                    out.append(s, i, tag.end);
                    i = tag.end;
                    continue;
                }
            } else if (c == '&' || c == SECTION) {
                if (bungee.region(i, n).lookingAt()) {
                    out.append(hexToMM(bungeeHexDigits(bungee.group())));
                    i = bungee.end();
                    continue;
                }
                if (ampHex.region(i, n).lookingAt()) {
                    out.append(hexToMM(ampHex.group(1)));
                    i = ampHex.end();
                    continue;
                }
                if (code.region(i, n).lookingAt()) {
                    out.append(codeToMM(code.group(1).charAt(0)));
                    i = code.end();
                    continue;
                }
            } else if (c == '#') {
                if (bare.region(i, n).lookingAt()) {
                    out.append(hexToMM(bare.group(1)));
                    i = bare.end();
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    /** Makes untrusted text literal: no legacy code, hex or MiniMessage tag in it will be interpreted by {@link #toMM}. */
    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '<' || c == '&' || c == SECTION || c == '#') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    public static Rendered render(String trusted) {
        return MiniParser.parse(toMM(trusted));
    }

    public static String legacy(String trusted) {
        return render(trusted).toLegacy();
    }

    public static String plain(String trusted) {
        return render(trusted).plain();
    }

    public static void send(CommandSender to, Rendered r) {
        if (to == null) return;
        if (to instanceof Player && r.needsComponents()) {
            ((Player) to).spigot().sendMessage(r.toComponents());
            return;
        }
        String legacy = r.toLegacy();
        if (legacy.isEmpty()) return;
        to.sendMessage(legacy);
    }
}
