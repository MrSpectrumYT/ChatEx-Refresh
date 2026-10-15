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

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Scheduler wrapper. The plugin is compiled against the 1.18 API, so every Folia scheduler
 * call goes through reflection. On Spigot / Paper the normal Bukkit scheduler is used.
 */
public class FoliaTaskManager {

    private static final String PKG = "io.papermc.paper.threadedregions.scheduler.";

    private final JavaPlugin plugin;
    private final boolean folia;

    private Object globalScheduler;
    private Object asyncScheduler;
    private Method globalRun;
    private Method globalRunDelayed;
    private Method globalCancel;
    private Method asyncRunNow;
    private Method asyncRunDelayed;
    private Method asyncCancel;
    private Method entityGetScheduler;
    private Method entityRun;

    public FoliaTaskManager(JavaPlugin plugin) {
        this.plugin = plugin;
        boolean detected = detectFolia();
        if (detected) {
            try {
                initFolia();
            } catch (ReflectiveOperationException | RuntimeException ex) {
                plugin.getLogger().log(Level.SEVERE, "Folia detected, but its scheduler API could not be accessed!", ex);
            }
        }
        this.folia = detected;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void initFolia() throws ReflectiveOperationException {
        Class<?> global = Class.forName(PKG + "GlobalRegionScheduler");
        Class<?> async = Class.forName(PKG + "AsyncScheduler");
        Class<?> entity = Class.forName(PKG + "EntityScheduler");

        globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
        asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);

        globalRun = global.getMethod("run", Plugin.class, Consumer.class);
        globalRunDelayed = global.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
        globalCancel = global.getMethod("cancelTasks", Plugin.class);

        asyncRunNow = async.getMethod("runNow", Plugin.class, Consumer.class);
        asyncRunDelayed = async.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
        asyncCancel = async.getMethod("cancelTasks", Plugin.class);

        entityGetScheduler = Entity.class.getMethod("getScheduler");
        entityRun = entity.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
    }

    public boolean isFolia() {
        return folia;
    }

    private void call(Method m, Object target, Object... args) {
        try {
            m.invoke(target, args);
        } catch (InvocationTargetException ex) {
            plugin.getLogger().log(Level.WARNING, "Scheduled task failed", ex.getCause());
        } catch (IllegalAccessException | RuntimeException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not schedule a task", ex);
        }
    }

    /** Runs on the main thread (Folia: global region thread). */
    public void runTask(Runnable task) {
        if (!plugin.isEnabled()) return;
        if (folia) {
            Consumer<Object> c = t -> task.run();
            call(globalRun, globalScheduler, plugin, c);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskLater(Runnable task, long delayTicks) {
        if (!plugin.isEnabled()) return;
        if (folia) {
            Consumer<Object> c = t -> task.run();
            call(globalRunDelayed, globalScheduler, plugin, c, Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void runTaskAsynchronously(Runnable task) {
        if (!plugin.isEnabled()) return;
        if (folia) {
            Consumer<Object> c = t -> task.run();
            call(asyncRunNow, asyncScheduler, plugin, c);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runTaskLaterAsynchronously(Runnable task, long delayTicks) {
        if (!plugin.isEnabled()) return;
        if (folia) {
            Consumer<Object> c = t -> task.run();
            call(asyncRunDelayed, asyncScheduler, plugin, c, Math.max(1L, delayTicks) * 50L, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    /**
     * Runs a task on the thread that owns the entity (Folia: its region, otherwise the main thread).
     * If we already are on the main thread of a normal server the task runs immediately.
     */
    public void runForEntity(Entity entity, Runnable task) {
        if (entity == null || !plugin.isEnabled()) return;
        if (folia) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                Consumer<Object> c = t -> task.run();
                entityRun.invoke(scheduler, plugin, c, null);
            } catch (InvocationTargetException ex) {
                plugin.getLogger().log(Level.WARNING, "Scheduled entity task failed", ex.getCause());
            } catch (IllegalAccessException | RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Could not schedule an entity task", ex);
            }
        } else if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void cancelAll() {
        if (folia) {
            if (globalCancel != null) call(globalCancel, globalScheduler, plugin);
            if (asyncCancel != null) call(asyncCancel, asyncScheduler, plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}
