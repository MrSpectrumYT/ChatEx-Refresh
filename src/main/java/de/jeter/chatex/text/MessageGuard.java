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

import de.jeter.chatex.utils.Locales;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Checks a text written by a player against the chat permissions and converts it to MiniMessage.
 * <p>
 * Everything that is recognised (legacy codes, hex, MiniMessage tags) needs its permission.
 * Text that is not a known code/tag is kept as plain text, so "&lt;3" or "a &lt; b" are fine.
 */
public final class MessageGuard {

    /** Every kind of formatting a player can be denied. */
    public enum Check {
        // legacy syntax: &c &l &#RRGGBB &x&f... &k
        L_COLOR("chatex.chat.colorlegacy", Locales.NO_COLOR_PERMISSION),
        L_MODIFIER("chatex.chat.colormodifier", Locales.NO_MODIFIER_PERMISSION),
        L_HEX("chatex.chat.colorhex", Locales.NO_HEX_PERMISSION),
        L_MAGIC("chatex.chat.colormagic", Locales.MAGIC_BLOCKED),
        // MiniMessage syntax
        M_COLOR("chatex.chat.minimessagelegacy", Locales.MM_NO_COLOR),
        M_MODIFIER("chatex.chat.minimessagemodifier", Locales.MM_NO_MODIFIER),
        M_HEX("chatex.chat.minimessagehex", Locales.MM_NO_HEX),
        M_MAGIC("chatex.chat.minimessagemagic", Locales.MM_NO_MAGIC),
        M_GRADIENT("chatex.chat.minimessagegradient", Locales.MM_NO_GRADIENT),
        M_RAINBOW("chatex.chat.minimessagerainbow", Locales.MM_NO_RAINBOW),
        M_URL("chatex.chat.minimessageurl", Locales.MM_NO_URL),
        M_SUGGEST("chatex.chat.minimessagesuggest", Locales.MM_NO_SUGGEST),
        M_COMMAND("chatex.chat.minimessagecommand", Locales.MM_NO_COMMAND),
        M_COPY("chatex.chat.minimessagecopy", Locales.MM_NO_COPY),
        M_HOVER("chatex.chat.minimessagehover", Locales.MM_NO_HOVER),
        M_INSERTION("chatex.chat.minimessageinsertion", Locales.MM_NO_INSERTION),
        M_KEYBIND("chatex.chat.minimessagekeybind", Locales.MM_NO_KEYBIND),
        M_FONT("chatex.chat.minimessagefont", Locales.MM_NO_FONT),
        M_LANG("chatex.chat.minimessagelang", Locales.MM_NO_LANG),
        M_NEWLINE("chatex.chat.minimessagenewline", Locales.MM_NO_NEWLINE);

        private final String permission;
        private final Locales message;

        Check(String permission, Locales message) {
            this.permission = permission;
            this.message = message;
        }

        public String permission() {
            return permission;
        }

        public Locales message() {
            return message;
        }
    }

    public static final class Result {
        private final String mm;
        private final Set<Check> denied;
        private final boolean changed;
        private final boolean usedStyle;

        Result(String mm, Set<Check> denied, boolean changed, boolean usedStyle) {
            this.mm = mm;
            this.denied = denied;
            this.changed = changed;
            this.usedStyle = usedStyle;
        }

        /**
         * True if the text contained a color or a text modifier (legacy codes, hex, &lt;red&gt;, &lt;bold&gt;,
         * &lt;gradient&gt;, &lt;rainbow&gt;, &lt;obfuscated&gt;, &lt;reset&gt;). Hover, click, newline... do not count.
         */
        public boolean usedStyle() {
            return usedStyle;
        }

        /** The checked message as MiniMessage. Plain text is escaped. Only valid if nothing was denied. */
        public String mm() {
            return mm;
        }

        public Set<Check> denied() {
            return denied;
        }

        /** True if the text contained any code or tag. */
        public boolean changed() {
            return changed;
        }
    }

    private static final int MAX_DEPTH = 3;

    private MessageGuard() {
    }

    public static Result process(String raw, Player player) {
        Set<Check> denied = new LinkedHashSet<>();
        boolean[] changed = new boolean[2];
        StringBuilder out = new StringBuilder(raw.length() + 16);
        scan(raw, player, 0, out, denied, changed, new EnumMap<>(Check.class));
        return new Result(out.toString(), denied, changed[0], changed[1]);
    }

    private static boolean allowed(Player p, Check c, Map<Check, Boolean> cache) {
        Boolean b = cache.get(c);
        if (b == null) {
            b = p.hasPermission(c.permission);
            cache.put(c, b);
        }
        return b;
    }

    private static void need(Player p, Check c, Set<Check> denied, Map<Check, Boolean> cache) {
        if (!allowed(p, c, cache)) denied.add(c);
    }

    private static String quote(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static boolean autoClosed(MiniParser.Kind k) {
        return k == MiniParser.Kind.GRADIENT || k == MiniParser.Kind.RAINBOW || k == MiniParser.Kind.CLICK
                || k == MiniParser.Kind.HOVER || k == MiniParser.Kind.INSERTION || k == MiniParser.Kind.FONT;
    }

    private static String closerFor(MiniParser.Tag t) {
        switch (t.kind) {
            case COLOR:
                return "</color>";
            case DECO:
                return decoCloser(t.deco);
            default:
                return "</" + t.kind.name().toLowerCase() + ">";
        }
    }

    private static Check checkFor(MiniParser.Tag t) {
        switch (t.kind) {
            case COLOR:
                return t.hex ? Check.M_HEX : Check.M_COLOR;
            case DECO:
                return t.deco == MiniParser.OBFUSCATED ? Check.M_MAGIC : Check.M_MODIFIER;
            case RESET:
                return Check.M_MODIFIER;
            case GRADIENT:
                return Check.M_GRADIENT;
            case RAINBOW:
                return Check.M_RAINBOW;
            case CLICK:
                switch (t.action) {
                    case "open_url":
                        return Check.M_URL;
                    case "run_command":
                        return Check.M_COMMAND;
                    case "suggest_command":
                        return Check.M_SUGGEST;
                    default:
                        return Check.M_COPY;
                }
            case HOVER:
                return Check.M_HOVER;
            case INSERTION:
                return Check.M_INSERTION;
            case FONT:
                return Check.M_FONT;
            case NEWLINE:
                return Check.M_NEWLINE;
            case KEYBIND:
                return Check.M_KEYBIND;
            case LANG:
                return Check.M_LANG;
            default:
                return Check.M_MODIFIER;
        }
    }

    private static void scan(String raw, Player p, int depth, StringBuilder out, Set<Check> denied,
                             boolean[] changed, Map<Check, Boolean> cache) {
        int n = raw.length();
        Matcher bungee = Fmt.BUNGEE_HEX.matcher(raw);
        Matcher ampHex = Fmt.AMP_HEX.matcher(raw);
        Matcher bare = Fmt.BARE_HEX.matcher(raw);
        Matcher code = Fmt.CODE.matcher(raw);
        List<MiniParser.Tag> open = new ArrayList<>();

        int i = 0;
        while (i < n) {
            char c = raw.charAt(i);

            if (c == '&' || c == '\u00A7') {
                if (bungee.region(i, n).lookingAt()) {
                    changed[0] = true;
                    if (depth == 0) changed[1] = true; // colors inside a hover tooltip do not count
                    need(p, Check.L_HEX, denied, cache);
                    out.append(Fmt.hexToMM(Fmt.bungeeHexDigits(bungee.group())));
                    i = bungee.end();
                    continue;
                }
                if (ampHex.region(i, n).lookingAt()) {
                    changed[0] = true;
                    if (depth == 0) changed[1] = true; // colors inside a hover tooltip do not count
                    need(p, Check.L_HEX, denied, cache);
                    out.append(Fmt.hexToMM(ampHex.group(1)));
                    i = ampHex.end();
                    continue;
                }
                if (code.region(i, n).lookingAt()) {
                    changed[0] = true;
                    if (depth == 0) changed[1] = true; // colors inside a hover tooltip do not count
                    char k = Character.toLowerCase(code.group(1).charAt(0));
                    if (k == 'k') need(p, Check.L_MAGIC, denied, cache);
                    else if (k == 'r' || (k >= 'l' && k <= 'o')) need(p, Check.L_MODIFIER, denied, cache);
                    else need(p, Check.L_COLOR, denied, cache);
                    out.append(Fmt.codeToMM(k));
                    i = code.end();
                    continue;
                }
            } else if (c == '#') {
                if (bare.region(i, n).lookingAt()) {
                    changed[0] = true;
                    if (depth == 0) changed[1] = true; // colors inside a hover tooltip do not count
                    need(p, Check.L_HEX, denied, cache);
                    out.append(Fmt.hexToMM(bare.group(1)));
                    i = bare.end();
                    continue;
                }
            } else if (c == '<') {
                MiniParser.Tag t = MiniParser.scanTag(raw, i);
                if (t != null) {
                    changed[0] = true;
                    int tagStart = i;
                    i = t.end;
                    if (t.closing) {
                        // only closing tags that match a tag opened in this very message are kept,
                        // so a player can never close the tags of the format
                        for (int k = open.size() - 1; k >= 0; k--) {
                            if (MiniParser.closes(t, open.get(k))) {
                                while (open.size() > k) open.remove(open.size() - 1);
                                out.append(closerFor(t));
                                break;
                            }
                        }
                        continue;
                    }
                    need(p, checkFor(t), denied, cache);
                    if (t.kind == MiniParser.Kind.COLOR || t.kind == MiniParser.Kind.DECO
                            || t.kind == MiniParser.Kind.RESET || t.kind == MiniParser.Kind.GRADIENT
                            || t.kind == MiniParser.Kind.RAINBOW) {
                        if (depth == 0) changed[1] = true; // colors inside a hover tooltip do not count
                    }
                    if (t.kind == MiniParser.Kind.RESET) {
                        open.clear();
                        out.append("<reset>");
                    } else if (t.kind == MiniParser.Kind.HOVER) {
                        StringBuilder inner = new StringBuilder();
                        if (depth < MAX_DEPTH) {
                            scan(t.value, p, depth + 1, inner, denied, changed, cache);
                        }
                        out.append("<hover:show_text:").append(quote(inner.toString())).append('>');
                        open.add(t);
                    } else {
                        out.append(raw, tagStart, i);
                        if (t.kind != MiniParser.Kind.NEWLINE && t.kind != MiniParser.Kind.KEYBIND
                                && t.kind != MiniParser.Kind.LANG) {
                            open.add(t);
                        }
                    }
                    continue;
                }
                out.append("\\<");
                i++;
                continue;
            } else if (c == '\\') {
                out.append("\\\\");
                i++;
                continue;
            }
            out.append(c);
            i++;
        }

        for (int k = open.size() - 1; k >= 0; k--) {
            MiniParser.Tag t = open.get(k);
            if (autoClosed(t.kind)) out.append(closerFor(t));
        }
    }

    private static String decoCloser(int deco) {
        switch (deco) {
            case MiniParser.BOLD:
                return "</bold>";
            case MiniParser.ITALIC:
                return "</italic>";
            case MiniParser.UNDERLINED:
                return "</underlined>";
            case MiniParser.STRIKE:
                return "</strikethrough>";
            default:
                return "</obfuscated>";
        }
    }
}
