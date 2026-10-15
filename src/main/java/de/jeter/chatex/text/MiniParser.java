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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Small self-contained MiniMessage implementation.
 * <p>
 * It does not need Adventure, so it works the same on Spigot 1.18+, Paper and Folia.
 * Output is a flat list of styled leaves ({@link Rendered}) that can be turned into
 * BungeeCord components (click / hover / ...) or into a legacy section-sign string.
 */
public final class MiniParser {

    private MiniParser() {
    }

    public enum Kind { COLOR, DECO, RESET, GRADIENT, RAINBOW, CLICK, HOVER, INSERTION, FONT, NEWLINE, KEYBIND, LANG }

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINED = 4;
    public static final int STRIKE = 8;
    public static final int OBFUSCATED = 16;

    /** Legacy color names in the order of the codes 0-9a-f. */
    public static final String[] LEGACY_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    public static final String[] LEGACY_HEX = {
            "#000000", "#0000AA", "#00AA00", "#00AAAA", "#AA0000", "#AA00AA", "#FFAA00", "#AAAAAA",
            "#555555", "#5555FF", "#55FF55", "#55FFFF", "#FF5555", "#FF55FF", "#FFFF55", "#FFFFFF"};

    private static final Map<String, String> NAMED = new HashMap<>();
    static final Map<String, Character> HEX_TO_CODE = new HashMap<>();
    private static final Map<String, Integer> DECORATIONS = new HashMap<>();

    static {
        for (int i = 0; i < 16; i++) {
            NAMED.put(LEGACY_NAMES[i], LEGACY_HEX[i]);
            HEX_TO_CODE.put(LEGACY_HEX[i], Character.forDigit(i, 16));
        }
        NAMED.put("grey", "#AAAAAA");
        NAMED.put("dark_grey", "#555555");

        DECORATIONS.put("bold", BOLD);
        DECORATIONS.put("b", BOLD);
        DECORATIONS.put("italic", ITALIC);
        DECORATIONS.put("i", ITALIC);
        DECORATIONS.put("em", ITALIC);
        DECORATIONS.put("underlined", UNDERLINED);
        DECORATIONS.put("u", UNDERLINED);
        DECORATIONS.put("strikethrough", STRIKE);
        DECORATIONS.put("st", STRIKE);
        DECORATIONS.put("obfuscated", OBFUSCATED);
        DECORATIONS.put("obf", OBFUSCATED);
    }

    // ------------------------------------------------------------------------------------------
    // Tags
    // ------------------------------------------------------------------------------------------

    public static final class Tag {
        public int end;
        public boolean closing;
        public boolean negated;
        public Kind kind;
        /** COLOR: "#RRGGBB"; CLICK/HOVER/INSERTION/FONT/KEYBIND/LANG: the value. */
        public String value;
        /** CLICK: the action (open_url, run_command, suggest_command, copy_to_clipboard). */
        public String action;
        /** COLOR: true when written as hex (&lt;#rrggbb&gt; or &lt;color:#rrggbb&gt;). */
        public boolean hex;
        /** DECO: the decoration bit. */
        public int deco;
        public int[][] colors;
        public double phase;
        public boolean reverse;
    }

    /** Returns the canonical "#RRGGBB" (upper case) for a hex or named color, otherwise null. */
    public static String parseColor(String s) {
        if (s == null) return null;
        String l = s.toLowerCase(Locale.ROOT);
        if (l.length() == 7 && l.charAt(0) == '#') {
            for (int i = 1; i < 7; i++) {
                if (Character.digit(l.charAt(i), 16) < 0) return null;
            }
            return l.toUpperCase(Locale.ROOT);
        }
        return NAMED.get(l);
    }

    private static int[] rgb(String hex) {
        return new int[]{Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)};
    }

    private static String join(List<String> args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.size(); i++) {
            if (i > from) sb.append(':');
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    /**
     * Tries to read a MiniMessage tag that starts at {@code start} (which must be a '&lt;').
     * Returns null if it is not a (known and valid) tag, in that case the text is literal.
     */
    public static Tag scanTag(String s, int start) {
        int n = s.length();
        if (start >= n || s.charAt(start) != '<') return null;

        List<String> parts = new ArrayList<>(4);
        StringBuilder cur = new StringBuilder();
        char quote = 0;
        int end = -1;
        for (int i = start + 1; i < n; i++) {
            char c = s.charAt(i);
            if (quote != 0) {
                if (c == '\\' && i + 1 < n && (s.charAt(i + 1) == quote || s.charAt(i + 1) == '\\')) {
                    cur.append(s.charAt(++i));
                } else if (c == quote) {
                    quote = 0;
                } else {
                    cur.append(c);
                }
                continue;
            }
            if ((c == '\'' || c == '"') && cur.length() == 0) {
                quote = c;
                continue;
            }
            if (c == '<') return null;
            if (c == '>') {
                parts.add(cur.toString());
                end = i + 1;
                break;
            }
            if (c == ':') {
                parts.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (end < 0) return null;

        String head = parts.get(0);
        Tag t = new Tag();
        t.end = end;
        if (head.startsWith("/")) {
            t.closing = true;
            head = head.substring(1);
        }
        if (head.startsWith("!")) {
            t.negated = true;
            head = head.substring(1);
        }
        String name = head.toLowerCase(Locale.ROOT);
        if (name.isEmpty()) return null;
        List<String> args = parts.subList(1, parts.size());

        // ---- colors
        if (name.charAt(0) == '#') {
            String hex = parseColor(name);
            if (hex == null || t.negated) return null;
            t.kind = Kind.COLOR;
            t.value = hex;
            t.hex = true;
            return t;
        }
        String named = NAMED.get(name);
        if (named != null) {
            if (t.negated) return null;
            t.kind = Kind.COLOR;
            t.value = named;
            return t;
        }

        Integer deco = DECORATIONS.get(name);
        if (deco != null) {
            t.kind = Kind.DECO;
            t.deco = deco;
            if (t.closing) return t;
            if (args.size() == 1) {
                String a = args.get(0).toLowerCase(Locale.ROOT);
                if (a.equals("false")) t.negated = !t.negated;
                else if (!a.equals("true")) return null;
            } else if (args.size() > 1) {
                return null;
            }
            return t;
        }

        if (t.negated) return null;

        switch (name) {
            case "color":
            case "c":
            case "colour": {
                t.kind = Kind.COLOR;
                if (t.closing) return t;
                if (args.size() != 1) return null;
                String hex = parseColor(args.get(0));
                if (hex == null) return null;
                t.value = hex;
                t.hex = args.get(0).startsWith("#");
                return t;
            }
            case "reset":
                t.kind = Kind.RESET;
                return args.isEmpty() || t.closing ? t : null;
            case "gradient": {
                t.kind = Kind.GRADIENT;
                if (t.closing) return t;
                List<int[]> cols = new ArrayList<>();
                for (int k = 0; k < args.size(); k++) {
                    String a = args.get(k).trim();
                    String hex = parseColor(a);
                    if (hex != null) {
                        cols.add(rgb(hex));
                    } else if (k == args.size() - 1) {
                        try {
                            t.phase = Double.parseDouble(a);
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
                if (cols.isEmpty()) {
                    cols.add(rgb("#FFFFFF"));
                    cols.add(rgb("#000000"));
                }
                t.colors = cols.toArray(new int[0][]);
                return t;
            }
            case "rainbow": {
                t.kind = Kind.RAINBOW;
                if (t.closing) return t;
                if (args.size() > 1) return null;
                if (args.size() == 1) {
                    String a = args.get(0).trim();
                    if (a.startsWith("!")) {
                        t.reverse = true;
                        a = a.substring(1);
                    }
                    if (!a.isEmpty()) {
                        try {
                            t.phase = Double.parseDouble(a);
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    }
                }
                return t;
            }
            case "click": {
                t.kind = Kind.CLICK;
                if (t.closing) return t;
                if (args.size() < 2) return null;
                String action = args.get(0).toLowerCase(Locale.ROOT);
                if (!(action.equals("open_url") || action.equals("run_command")
                        || action.equals("suggest_command") || action.equals("copy_to_clipboard"))) {
                    return null;
                }
                String value = join(args, 1);
                if (value.isEmpty()) return null;
                if (action.equals("open_url")) {
                    String lv = value.toLowerCase(Locale.ROOT);
                    if (!(lv.startsWith("http://") || lv.startsWith("https://"))) return null;
                }
                t.action = action;
                t.value = value;
                return t;
            }
            case "hover": {
                t.kind = Kind.HOVER;
                if (t.closing) return t;
                if (args.size() < 2 || !args.get(0).equalsIgnoreCase("show_text")) return null;
                t.value = join(args, 1);
                return t;
            }
            case "insertion": {
                t.kind = Kind.INSERTION;
                if (t.closing) return t;
                if (args.isEmpty()) return null;
                t.value = join(args, 0);
                return t;
            }
            case "font": {
                t.kind = Kind.FONT;
                if (t.closing) return t;
                if (args.isEmpty()) return null;
                t.value = join(args, 0);
                return t;
            }
            case "newline":
            case "br":
                t.kind = Kind.NEWLINE;
                return args.isEmpty() ? t : null;
            case "key":
            case "keybind": {
                t.kind = Kind.KEYBIND;
                if (args.size() != 1 || args.get(0).isEmpty()) return null;
                t.value = args.get(0);
                return t;
            }
            case "lang":
            case "tr":
            case "translatable": {
                t.kind = Kind.LANG;
                if (args.isEmpty() || args.get(0).isEmpty()) return null;
                t.value = args.get(0);
                return t;
            }
            default:
                return null;
        }
    }

    /** True if the closing tag {@code close} closes the opened tag {@code open}. */
    public static boolean closes(Tag close, Tag open) {
        if (close.kind != open.kind) return false;
        return close.kind != Kind.DECO || close.deco == open.deco;
    }

    // ------------------------------------------------------------------------------------------
    // Tree
    // ------------------------------------------------------------------------------------------

    private static final class Node {
        final Tag tag;
        final String text;
        final List<Node> kids = new ArrayList<>();

        Node(Tag tag, String text) {
            this.tag = tag;
            this.text = text;
        }
    }

    private static void flush(StringBuilder text, Node into) {
        if (text.length() > 0) {
            into.kids.add(new Node(null, text.toString()));
            text.setLength(0);
        }
    }

    public static Rendered parse(String s) {
        if (s == null) s = "";
        Node root = new Node(null, null);
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        StringBuilder text = new StringBuilder();

        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < n && (s.charAt(i + 1) == '<' || s.charAt(i + 1) == '\\')) {
                text.append(s.charAt(i + 1));
                i += 2;
                continue;
            }
            if (c == '<') {
                Tag t = scanTag(s, i);
                if (t != null) {
                    flush(text, stack.peek());
                    apply(t, stack, root);
                    i = t.end;
                    continue;
                }
            }
            text.append(c);
            i++;
        }
        flush(text, stack.peek());

        List<Leaf> leaves = new ArrayList<>();
        walk(root, Style.EMPTY, leaves);
        return new Rendered(leaves);
    }

    private static void apply(Tag t, Deque<Node> stack, Node root) {
        if (t.closing) {
            for (Node open : stack) {
                if (open != root && closes(t, open.tag)) {
                    while (stack.peek() != open) stack.pop();
                    stack.pop();
                    return;
                }
            }
            return;
        }
        switch (t.kind) {
            case RESET:
                stack.clear();
                stack.push(root);
                return;
            case NEWLINE:
            case KEYBIND:
            case LANG:
                stack.peek().kids.add(new Node(t, null));
                return;
            default: {
                Node node = new Node(t, null);
                stack.peek().kids.add(node);
                stack.push(node);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------------------------

    interface Provider {
        String next();
    }

    private static String hex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static final class GradientProvider implements Provider {
        private final int[][] cols;
        private final int total;
        private final double phase;
        private int idx;

        GradientProvider(int[][] cols, int total, double phase) {
            this.cols = cols;
            this.total = total;
            this.phase = phase;
        }

        @Override
        public String next() {
            double t = total <= 1 ? 0 : (double) idx / (total - 1);
            idx++;
            if (phase != 0) {
                t = t + phase;
                t = t - Math.floor(t);
            }
            int k = cols.length;
            if (k == 1) return hex(cols[0][0], cols[0][1], cols[0][2]);
            double scaled = t * (k - 1);
            int seg = Math.min((int) scaled, k - 2);
            double f = scaled - seg;
            int[] a = cols[seg];
            int[] b = cols[seg + 1];
            return hex((int) Math.round(a[0] + f * (b[0] - a[0])),
                    (int) Math.round(a[1] + f * (b[1] - a[1])),
                    (int) Math.round(a[2] + f * (b[2] - a[2])));
        }
    }

    private static final class RainbowProvider implements Provider {
        private final int total;
        private final double phase;
        private final boolean reverse;
        private int idx;

        RainbowProvider(int total, double phase, boolean reverse) {
            this.total = Math.max(total, 1);
            this.phase = phase;
            this.reverse = reverse;
        }

        @Override
        public String next() {
            double h = (double) idx / total;
            idx++;
            if (reverse) h = 1.0 - h;
            h = h + phase / total;
            h = h - Math.floor(h);
            return hsv(h);
        }

        private static String hsv(double h) {
            double x = h * 6.0;
            int sector = (int) Math.floor(x) % 6;
            double f = x - Math.floor(x);
            double q = 1.0 - f;
            double r, g, b;
            switch (sector) {
                case 0: r = 1; g = f; b = 0; break;
                case 1: r = q; g = 1; b = 0; break;
                case 2: r = 0; g = 1; b = f; break;
                case 3: r = 0; g = q; b = 1; break;
                case 4: r = f; g = 0; b = 1; break;
                default: r = 1; g = 0; b = q; break;
            }
            return hex((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255));
        }
    }

    static final class Style {
        static final Style EMPTY = new Style(null, 0, null, null, null, null, null, null);

        final String color;
        final int deco;
        final String clickAct;
        final String clickVal;
        final String hover;
        final String insertion;
        final String font;
        final Provider provider;

        Style(String color, int deco, String clickAct, String clickVal, String hover, String insertion,
              String font, Provider provider) {
            this.color = color;
            this.deco = deco;
            this.clickAct = clickAct;
            this.clickVal = clickVal;
            this.hover = hover;
            this.insertion = insertion;
            this.font = font;
            this.provider = provider;
        }

        Style withColor(String c) {
            return new Style(c, deco, clickAct, clickVal, hover, insertion, font, provider);
        }

        Style withDeco(int bit, boolean on) {
            return new Style(color, on ? deco | bit : deco & ~bit, clickAct, clickVal, hover, insertion, font, provider);
        }

        Style withProvider(Provider p) {
            return new Style(null, deco, clickAct, clickVal, hover, insertion, font, p);
        }

        Style withClick(String a, String v) {
            return new Style(color, deco, a, v, hover, insertion, font, provider);
        }

        Style withHover(String h) {
            return new Style(color, deco, clickAct, clickVal, h, insertion, font, provider);
        }

        Style withInsertion(String i) {
            return new Style(color, deco, clickAct, clickVal, hover, i, font, provider);
        }

        Style withFont(String f) {
            return new Style(color, deco, clickAct, clickVal, hover, insertion, f, provider);
        }

        Style resolved(String c) {
            return new Style(c, deco, clickAct, clickVal, hover, insertion, font, null);
        }

        boolean sameAs(Style o) {
            return deco == o.deco && Objects.equals(color, o.color) && Objects.equals(clickAct, o.clickAct)
                    && Objects.equals(clickVal, o.clickVal) && Objects.equals(hover, o.hover)
                    && Objects.equals(insertion, o.insertion) && Objects.equals(font, o.font);
        }

        boolean hasEvents() {
            return clickAct != null || hover != null || insertion != null || font != null;
        }
    }

    static final class Leaf {
        static final int TEXT = 0;
        static final int KEYBIND = 1;
        static final int LANG = 2;

        final String text;
        final int type;
        final Style style;

        Leaf(String text, int type, Style style) {
            this.text = text;
            this.type = type;
            this.style = style;
        }
    }

    private static int count(Node node) {
        int total = 0;
        for (Node k : node.kids) {
            if (k.tag == null) {
                total += k.text.codePointCount(0, k.text.length());
            } else if (k.tag.kind == Kind.KEYBIND || k.tag.kind == Kind.LANG) {
                total++;
            } else if (k.tag.kind != Kind.NEWLINE) {
                total += count(k);
            }
        }
        return total;
    }

    private static String color(Style st) {
        if (st.provider == null) return st.color;
        String c = st.provider.next();
        return st.color != null ? st.color : c;
    }

    private static void walk(Node node, Style st, List<Leaf> out) {
        for (Node k : node.kids) {
            if (k.tag == null) {
                if (st.provider == null) {
                    out.add(new Leaf(k.text, Leaf.TEXT, st.resolved(st.color)));
                } else {
                    for (int i = 0; i < k.text.length(); ) {
                        int cp = k.text.codePointAt(i);
                        int len = Character.charCount(cp);
                        out.add(new Leaf(k.text.substring(i, i + len), Leaf.TEXT, st.resolved(color(st))));
                        i += len;
                    }
                }
                continue;
            }
            Tag t = k.tag;
            switch (t.kind) {
                case COLOR:
                    walk(k, st.withColor(t.value), out);
                    break;
                case DECO:
                    walk(k, st.withDeco(t.deco, !t.negated), out);
                    break;
                case GRADIENT:
                    walk(k, st.withProvider(new GradientProvider(t.colors, count(k), t.phase)), out);
                    break;
                case RAINBOW:
                    walk(k, st.withProvider(new RainbowProvider(count(k), t.phase, t.reverse)), out);
                    break;
                case CLICK:
                    walk(k, st.withClick(t.action, t.value), out);
                    break;
                case HOVER:
                    walk(k, st.withHover(t.value), out);
                    break;
                case INSERTION:
                    walk(k, st.withInsertion(t.value), out);
                    break;
                case FONT:
                    walk(k, st.withFont(t.value), out);
                    break;
                case NEWLINE:
                    out.add(new Leaf("\n", Leaf.TEXT, st.resolved(st.color)));
                    break;
                case KEYBIND:
                    out.add(new Leaf(t.value, Leaf.KEYBIND, st.resolved(color(st))));
                    break;
                case LANG:
                    out.add(new Leaf(t.value, Leaf.LANG, st.resolved(color(st))));
                    break;
                default:
                    break;
            }
        }
    }
}
