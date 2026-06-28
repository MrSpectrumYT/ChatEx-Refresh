package de.jeter.chatex.utils.adManager;

import de.jeter.chatex.utils.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartAdManager implements AdManager {
    private static final Map<UUID, Double> errorScore = new ConcurrentHashMap<>();
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[.,-:; ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9]))"
    );
    
    private static final Pattern WEB_PATTERN = Pattern.compile(
        "((([a-zA-Z0-9_-]{2,256}\\.)*)?[a-zA-Z0-9_-]{2,256}\\.[a-zA-Z_-]{2,256})(\\/[-a-zA-Z0-9@:%_\\\\+~#?&\\/=]*)?"
    );

    private static boolean checkForIP(String message) {
        message = message.replaceAll(" ", "");
        Matcher matcher = IP_PATTERN.matcher(message);
        
        while (matcher.find()) {
            if (matcher.group().length() == 0) continue;
            
            String text = matcher.group().trim()
                .replaceAll("http://", "")
                .replaceAll("https://", "")
                .split("/")[0];

            if (IP_PATTERN.matcher(text).find() && !Utils.checkForBypassString(text)) {
                return true;
            }
        }
        return false;
    }

    private static double checkForWeb(String message) {
        if (!message.contains(",") && !message.contains(".")) {
            return 0;
        }

        double messageLength = message.length();
        String processed = Config.ADS_REPLACE_COMMAS.getBoolean() 
            ? message.replaceAll(",", ".") 
            : message;
        
        String urlCompactor = "[\\(\\)\\]\\[]|([\\s:\\/](?=.{0," + Config.ADS_MAX_LENGTH.getInt() + "}[\\.]))|((?<=[\\.].{0,4})\\s*)";
        processed = processed.replaceAll(urlCompactor, "");
        
        Matcher matcher = WEB_PATTERN.matcher(processed);
        double error = 0;

        while (matcher.find()) {
            if (matcher.group().length() == 0) continue;
            
            String text = matcher.group().trim();
            if (Utils.checkForBypassString(text)) continue;
            
            error += text.length();
            if (DomainDictionary.containsTopLevelEnding(text)) {
                error *= Config.ADS_SMART_MULTIPLIER.getDouble();
            } else {
                error *= Config.ADS_SMART_UN_MULTIPLIER.getDouble();
            }
        }

        return error > 0 ? error / messageLength : 0;
    }

    @Override
    public boolean checkForAds(String message, Player player) {
        if (player.hasPermission("chatex.bypassads") || !Config.ADS_ENABLED.getBoolean()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        errorScore.putIfAbsent(uuid, 0.0);
        
        double error = checkForWeb(message);
        errorScore.compute(uuid, (key, current) -> Math.max(0, current + error));
        
        boolean canceled = errorScore.get(uuid) > Config.ADS_THRESHOLD.getDouble() || checkForIP(message);
        
        if (canceled) {
            errorScore.put(uuid, Config.ADS_THRESHOLD.getDouble());
            
            String notifyMsg = Locales.MESSAGES_AD_NOTIFY.getString(player)
                .replace("%player%", Matcher.quoteReplacement(player.getName()))
                .replace("%message%", Matcher.quoteReplacement(message));
            Utils.notifyOps(notifyMsg);
            ChatLogger.writeToAdFile(player, message);
        } else {
            double reduction = Config.ADS_REDUCE_THRESHOLD.getDouble();
            errorScore.compute(uuid, (key, current) -> Math.max(0, current - reduction));
        }
        
        return canceled;
    }

    public static void removePlayer(Player player) {
        errorScore.remove(player.getUniqueId());
    }
}