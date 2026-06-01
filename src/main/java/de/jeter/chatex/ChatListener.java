package de.jeter.chatex;

import de.jeter.chatex.api.events.*;
import de.jeter.chatex.plugins.PluginManager;
import de.jeter.chatex.utils.*;
import de.jeter.chatex.utils.adManager.AdManager;
import de.jeter.chatex.utils.adManager.SimpleAdManager;
import de.jeter.chatex.utils.adManager.SmartAdManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final AdManager adManager = Config.ADS_SMART_MANAGER.getBoolean() ? new SmartAdManager() : new SimpleAdManager();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLowest(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("LOWEST")) {
            executeChatEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLow(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("LOW")) {
            executeChatEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNormal(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("NORMAL")) {
            executeChatEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHigh(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("HIGH")) {
            executeChatEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHighest(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("HIGHEST")) {
            executeChatEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMonitor(final AsyncPlayerChatEvent event) {
        if (Config.PRIORITY.getString().equalsIgnoreCase("MONITOR")) {
            executeChatEvent(event);
        }
    }

    private void executeChatEvent(AsyncPlayerChatEvent event) {
        LogHelper.debug("ChatEvent fired with priority: " + Config.PRIORITY.getString().toUpperCase() + ", ChatEx-Refresh reacting to it...");
        Player player = event.getPlayer();

        if (!player.hasPermission("chatex.allowchat")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%perm", "chatex.allowchat");
            player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getComponent(player, placeholders));
            event.setCancelled(true);
            return;
        }

        String format = PluginManager.getInstance().getMessageFormat(event.getPlayer());
        LogHelper.debug("Format: " + format);
        LogHelper.debug("Prefix: " + PluginManager.getInstance().getPrefix(event.getPlayer()));
        LogHelper.debug("Suffix: " + PluginManager.getInstance().getSuffix(event.getPlayer()));

        String chatMessage = event.getMessage();

        if (!AntiSpamManager.getInstance().isAllowed(event.getPlayer())) {
            long remainingTime = AntiSpamManager.getInstance().getRemainingSeconds(event.getPlayer());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%time%", String.valueOf(remainingTime));
            player.sendMessage(Locales.ANTI_SPAM_DENIED.getComponent(event.getPlayer(), placeholders));
            event.setCancelled(true);
            return;
        }
        AntiSpamManager.getInstance().put(player);

        LogHelper.debug("Player did not activate the AntiSpam. Continuing...");

        if (adManager.checkForAds(chatMessage, player)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%perm", "chatex.bypassads");
            player.sendMessage(Locales.MESSAGES_AD.getComponent(null, placeholders));
            event.setCancelled(true);
            return;
        }

        LogHelper.debug("Player did not activate the AdBlocker. Continuing...");

        for (String block : Config.BLOCKED_WORDS.getStringList()) {
            if (chatMessage.toLowerCase().contains(block.toLowerCase())) {
                LogHelper.debug("Player activated wordblocker! ChatMessage: " + chatMessage + " contains blockedWord: " + block);
                player.sendMessage(Locales.MESSAGES_BLOCKED.getComponent(null));
                event.setCancelled(true);
                return;
            }
        }

        LogHelper.debug("Player did not use a blocked word. Continuing...");
        LogHelper.debug("ChatMessage: " + chatMessage);
        boolean global = false;

        if (Config.RANGEMODE.getBoolean()) {
            LogHelper.debug("Message starts with prefix (" + Config.RANGEPREFIX.getString() + "): " + chatMessage.startsWith(Config.RANGEPREFIX.getString()));
            if (Config.RANGEMODE.getBoolean() && chatMessage.startsWith(Config.RANGEPREFIX.getString())) {
                LogHelper.debug("Global mode enabled!");
                if (player.hasPermission("chatex.chat.global")) {
                    chatMessage = chatMessage.replaceFirst(Pattern.quote(Config.RANGEPREFIX.getString()), "");
                    format = PluginManager.getInstance().getGlobalMessageFormat(player);
                    global = true;

                    PlayerUsesGlobalChatEvent playerUsesGlobalChatEvent = new PlayerUsesGlobalChatEvent(player, chatMessage);
                    Bukkit.getPluginManager().callEvent(playerUsesGlobalChatEvent);
                    chatMessage = playerUsesGlobalChatEvent.getMessage();
                    if (playerUsesGlobalChatEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%perm", "chatex.chat.global");
                    player.sendMessage(Locales.COMMAND_RESULT_NO_PERM.getComponent(player, placeholders));
                    event.setCancelled(true);
                    return;
                }
            } else {
                LogHelper.debug("Range mode enabled!");
                event.getRecipients().clear();
                if (Utils.getLocalRecipients(player).size() == 1 && Config.SHOW_NO_RECEIVER_MSG.getBoolean()) {
                    player.sendMessage(Locales.NO_LISTENING_PLAYERS.getComponent(player));
                    event.setCancelled(true);
                    return;
                } else {
                    event.getRecipients().addAll(Utils.getLocalRecipients(player));

                    PlayerUsesRangeModeEvent playerUsesRangeModeEvent = new PlayerUsesRangeModeEvent(player, chatMessage);
                    Bukkit.getPluginManager().callEvent(playerUsesRangeModeEvent);
                    chatMessage = playerUsesRangeModeEvent.getMessage();
                    if (playerUsesRangeModeEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        LogHelper.debug("Replacing Placeholder in format...");
        format = Utils.replacePlayerPlaceholders(player, format, false);
        format = Utils.escape(format);
        format = format.replace("%%message", "%2$s");
        format = Utils.replaceColors(format);
        LogHelper.debug("Format after replacing: " + format);

        try {
            event.setFormat(format);
        } catch (UnknownFormatConversionException ex) {
            System.out.println(format);
            ChatEx.getInstance().getLogger().severe("Placeholder in format is not allowed!");
            format = format.replaceAll("%\\\\?.*?%", "");
            event.setFormat(format);
        }

        String finalMessage = Utils.translateColorCodes(chatMessage, player);
        if (finalMessage == null) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(finalMessage);
        
        ChatLogger.writeToFile(player, chatMessage);
        LogHelper.debug("Everything done! Method end.");
    }
}