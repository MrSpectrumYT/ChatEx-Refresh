/*
 * This file is part of ChatEx Refresh
 * Copyright (C) 2026 MrSpectrumYT
 *
 * This file is part of ChatEx
 * Copyright (C) 2022 ChatEx Team
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
package de.jeter.chatex.plugins;

import de.jeter.chatex.ChatEx;
import de.jeter.chatex.utils.HookManager;
import org.bukkit.entity.Player;

public class PluginManager implements PermissionsPlugin {

    private static PermissionsPlugin handler;
    private static PluginManager INSTANCE;

    public static PermissionsPlugin getInstance() {
        return INSTANCE;
    }

    public static void load() {
        INSTANCE = new PluginManager();
        if (HookManager.checkLuckperms()) {
            handler = new LuckPerms();
            ChatEx.getInstance().getLogger().info("Successfully hooked into: " + handler.getName());
        } else {
            handler = new Nothing();
            ChatEx.getInstance().getLogger().info("No permissions plugin found. Using default: " + handler.getName());
        }

        if (HookManager.checkPlaceholderAPI()) {
            ChatEx.getInstance().getLogger().info("Hooked into PlaceholderAPI");
        }
    }

    @Override
    public String getName() {
        return handler.getName();
    }

    @Override
    public String getPrefix(Player p) {
        return handler.getPrefix(p);
    }

    @Override
    public String getSuffix(Player p) {
        return handler.getSuffix(p);
    }

    @Override
    public String[] getGroupNames(Player p) {
        return handler.getGroupNames(p);
    }

    @Override
    public String getMessageFormat(Player p) {
        return handler.getMessageFormat(p);
    }

    @Override
    public String getGlobalMessageFormat(Player p) {
        return handler.getGlobalMessageFormat(p);
    }
}