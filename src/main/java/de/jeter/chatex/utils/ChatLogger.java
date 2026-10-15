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
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Chat / ad log. Thread safe (chat is async) and rotates the chat log at midnight. */
public class ChatLogger {

    private static final Object LOCK = new Object();
    private static BufferedWriter chatWriter = null;
    private static String chatDay = "";
    private static BufferedWriter adWriter = null;

    public static void load() {
        synchronized (LOCK) {
            close();
            try {
                File logFolder = new File(ChatEx.getInstance().getDataFolder(), "logs");
                if (Config.LOGCHAT.getBoolean() || Config.ADS_LOG.getBoolean()) {
                    logFolder.mkdirs();
                }
                if (Config.ADS_LOG.getBoolean()) {
                    adWriter = new BufferedWriter(new FileWriter(new File(logFolder, "ads.log"), true));
                }
                openChat();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void openChat() throws IOException {
        if (!Config.LOGCHAT.getBoolean()) return;
        File logFolder = new File(ChatEx.getInstance().getDataFolder(), "logs");
        logFolder.mkdirs();
        chatDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        chatWriter = new BufferedWriter(new FileWriter(new File(logFolder, chatDay + ".log"), true));
    }

    public static void close() {
        synchronized (LOCK) {
            try {
                if (chatWriter != null) chatWriter.close();
                if (adWriter != null) adWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                chatWriter = null;
                adWriter = null;
            }
        }
    }

    public static void writeToFile(Player player, String message) {
        if (!Config.LOGCHAT.getBoolean()) return;
        synchronized (LOCK) {
            try {
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if (chatWriter == null || !today.equals(chatDay)) {
                    if (chatWriter != null) chatWriter.close();
                    openChat();
                }
                if (chatWriter == null) return;
                chatWriter.write(prefix(false) + player.getName() + " (uuid: " + player.getUniqueId() + "): " + message);
                chatWriter.newLine();
                chatWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeToAdFile(Player player, String message) {
        if (!Config.ADS_LOG.getBoolean()) return;
        synchronized (LOCK) {
            if (adWriter == null) return;
            try {
                adWriter.write(prefix(true) + player.getName() + " (uuid: " + player.getUniqueId() + "): " + message);
                adWriter.newLine();
                adWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String prefix(boolean day) {
        return new SimpleDateFormat(day ? "[yyyy-MM-dd HH:mm:ss] " : "[HH:mm:ss] ").format(new Date());
    }
}
