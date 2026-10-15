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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of parsing a MiniMessage string: a flat list of styled pieces of text.
 */
public final class Rendered {

    private static final char SECTION = '\u00A7';

    private final List<MiniParser.Leaf> leaves;

    Rendered(List<MiniParser.Leaf> leaves) {
        this.leaves = leaves;
    }

    /** True if the result contains click/hover/insertion/font/keybind/translatable parts. */
    public boolean needsComponents() {
        for (MiniParser.Leaf l : leaves) {
            if (l.type != MiniParser.Leaf.TEXT || l.style.hasEvents()) return true;
        }
        return false;
    }

    public String plain() {
        StringBuilder sb = new StringBuilder();
        for (MiniParser.Leaf l : leaves) sb.append(l.text);
        return sb.toString();
    }

    /** Section sign string. Events (click/hover) can not be represented and are dropped. */
    public String toLegacy() {
        StringBuilder sb = new StringBuilder();
        String curColor = null;
        int curDeco = 0;

        for (MiniParser.Leaf l : leaves) {
            if (l.text.isEmpty()) continue;
            String c = l.style.color;
            int d = l.style.deco;

            if (c == null) {
                if (curColor != null || (curDeco & ~d) != 0) {
                    sb.append(SECTION).append('r');
                    curColor = null;
                    curDeco = 0;
                }
            } else {
                boolean needColor = !c.equals(curColor);
                if (!needColor && (curDeco & ~d) != 0) {
                    sb.append(SECTION).append('r');
                    curDeco = 0;
                    needColor = true;
                }
                if (needColor) {
                    appendColor(sb, c);
                    curColor = c;
                    curDeco = 0;
                }
            }

            int add = d & ~curDeco;
            if ((add & MiniParser.BOLD) != 0) sb.append(SECTION).append('l');
            if ((add & MiniParser.ITALIC) != 0) sb.append(SECTION).append('o');
            if ((add & MiniParser.UNDERLINED) != 0) sb.append(SECTION).append('n');
            if ((add & MiniParser.STRIKE) != 0) sb.append(SECTION).append('m');
            if ((add & MiniParser.OBFUSCATED) != 0) sb.append(SECTION).append('k');
            curDeco |= add;

            sb.append(l.text);
        }
        return sb.toString();
    }

    private static void appendColor(StringBuilder sb, String hex) {
        Character code = MiniParser.HEX_TO_CODE.get(hex);
        if (code != null) {
            sb.append(SECTION).append(code);
            return;
        }
        sb.append(SECTION).append('x');
        for (int i = 1; i < 7; i++) {
            sb.append(SECTION).append(Character.toLowerCase(hex.charAt(i)));
        }
    }

    public BaseComponent[] toComponents() {
        return toComponents(0);
    }

    BaseComponent[] toComponents(int depth) {
        List<BaseComponent> out = new ArrayList<>();
        int i = 0;
        while (i < leaves.size()) {
            MiniParser.Leaf l = leaves.get(i);
            if (l.text.isEmpty()) {
                i++;
                continue;
            }
            if (l.type == MiniParser.Leaf.TEXT) {
                StringBuilder sb = new StringBuilder(l.text);
                int j = i + 1;
                while (j < leaves.size() && leaves.get(j).type == MiniParser.Leaf.TEXT
                        && leaves.get(j).style.sameAs(l.style)) {
                    sb.append(leaves.get(j).text);
                    j++;
                }
                out.add(build(new TextComponent(sb.toString()), l.style, depth));
                i = j;
            } else {
                BaseComponent bc = l.type == MiniParser.Leaf.KEYBIND
                        ? new KeybindComponent(l.text)
                        : new TranslatableComponent(l.text);
                out.add(build(bc, l.style, depth));
                i++;
            }
        }
        return out.toArray(new BaseComponent[0]);
    }

    private static BaseComponent build(BaseComponent c, MiniParser.Style s, int depth) {
        if (s.color != null) {
            Character code = MiniParser.HEX_TO_CODE.get(s.color);
            c.setColor(code != null ? ChatColor.getByChar(code) : ChatColor.of(s.color));
        }
        if ((s.deco & MiniParser.BOLD) != 0) c.setBold(true);
        if ((s.deco & MiniParser.ITALIC) != 0) c.setItalic(true);
        if ((s.deco & MiniParser.UNDERLINED) != 0) c.setUnderlined(true);
        if ((s.deco & MiniParser.STRIKE) != 0) c.setStrikethrough(true);
        if ((s.deco & MiniParser.OBFUSCATED) != 0) c.setObfuscated(true);

        if (s.clickAct != null) {
            ClickEvent.Action action;
            switch (s.clickAct) {
                case "open_url":
                    action = ClickEvent.Action.OPEN_URL;
                    break;
                case "run_command":
                    action = ClickEvent.Action.RUN_COMMAND;
                    break;
                case "suggest_command":
                    action = ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                default:
                    action = ClickEvent.Action.COPY_TO_CLIPBOARD;
                    break;
            }
            c.setClickEvent(new ClickEvent(action, s.clickVal));
        }
        if (s.hover != null && depth < 2) {
            BaseComponent[] hover = MiniParser.parse(Fmt.toMM(s.hover)).toComponents(depth + 1);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
        }
        if (s.insertion != null) c.setInsertion(s.insertion);
        if (s.font != null) {
            try {
                BaseComponent.class.getMethod("setFont", String.class).invoke(c, s.font);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // very old bungee chat api without font support
            }
        }
        return c;
    }
}
