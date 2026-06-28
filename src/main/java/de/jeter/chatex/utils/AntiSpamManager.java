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