package de.jeter.chatex.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AntiSpamManager {

    private static final AntiSpamManager instance = new AntiSpamManager();
    private final Map<Player, Long> map = new HashMap<>();

    private AntiSpamManager() {

    }

    public static AntiSpamManager getInstance() {
        return instance;
    }

    public void put(Player chatter) {
        map.put(chatter, System.currentTimeMillis());
    }

    public boolean isAllowed(Player chatter) {
        if (!map.containsKey(chatter) || !Config.ANTISPAM_ENABLED.getBoolean() || chatter.hasPermission("chatex.antispam.bypass")) {
            return true;
        }

        long lastChat = map.get(chatter) + (Config.ANTISPAM_SECONDS.getInt() * 1000L);
        long current = System.currentTimeMillis();

        return current > lastChat;
    }

    public long getRemainingSeconds(Player chatter) {
        if (isAllowed(chatter)) {
            return 0;
        }

        long lastChat = map.get(chatter) + (Config.ANTISPAM_SECONDS.getInt() * 1000L);
        long current = System.currentTimeMillis();

        long diff = lastChat - current;
        return TimeUnit.MILLISECONDS.toSeconds(diff);
    }

    public void clear() {
        map.clear();
    }

}
