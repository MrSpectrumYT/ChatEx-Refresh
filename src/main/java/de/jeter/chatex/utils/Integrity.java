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

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Reports whether config.yml, the selected locale and the database (userdata.yml) could be read. */
public final class Integrity {

    private Integrity() {
    }

    /** First line of an error, short enough for a chat/console line. */
    public static String shortError(Throwable t) {
        String m = t.getMessage();
        if (m == null || m.isEmpty()) m = t.getClass().getSimpleName();
        int nl = m.indexOf('\n');
        if (nl > 0) m = m.substring(0, nl);
        m = m.trim();
        return m.length() > 200 ? m.substring(0, 200) + "..." : m;
    }

    public static boolean allOk() {
        return Config.isLoadOk() && Locales.isLoadOk() && ColorManager.isLoadOk();
    }

    private static Map<String, String> values(String file, String error) {
        Map<String, String> values = new HashMap<>();
        values.put("%file%", file);
        values.put("%error%", error);
        return values;
    }

    private static Locales[] messages() {
        return new Locales[]{
                Config.isLoadOk() ? Locales.INTEGRITY_CONFIG_OK : Locales.INTEGRITY_CONFIG_FAILED,
                Locales.isLoadOk() ? Locales.INTEGRITY_LOCALE_OK : Locales.INTEGRITY_LOCALE_FAILED,
                ColorManager.isLoadOk() ? Locales.INTEGRITY_DATABASE_OK : Locales.INTEGRITY_DATABASE_FAILED};
    }

    private static Map<String, String>[] params() {
        @SuppressWarnings("unchecked")
        Map<String, String>[] p = new Map[3];
        p[0] = values("config.yml", Config.getLoadError());
        p[1] = values(Locales.getLoadedFileName(), Locales.getLoadError());
        p[2] = values(ColorManager.getFileName(), ColorManager.getLoadError());
        return p;
    }

    /** Console report (3 lines). */
    public static void report(Logger log) {
        Locales[] m = messages();
        Map<String, String>[] p = params();
        for (int i = 0; i < m.length; i++) {
            String line = m[i].getPlain(p[i]);
            if (m[i].name().endsWith("_FAILED")) log.severe(line);
            else log.info(line);
        }
    }

    /** Report to a command sender (only the files that failed). */
    public static void reportFailures(CommandSender to) {
        Locales[] m = messages();
        Map<String, String>[] p = params();
        for (int i = 0; i < m.length; i++) {
            if (m[i].name().endsWith("_FAILED")) m[i].send(to, p[i]);
        }
    }
}
