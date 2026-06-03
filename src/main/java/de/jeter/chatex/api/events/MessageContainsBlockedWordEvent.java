package de.jeter.chatex.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageContainsBlockedWordEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private String message;
    private String pluginMessage;
    private boolean canceled = false;

    public MessageContainsBlockedWordEvent(Player player, String message, String pluginMessage) {
        super(true);
        this.player = player;
        this.message = message;
        this.pluginMessage = pluginMessage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPluginMessage() {
        return pluginMessage;
    }

    public void setPluginMessage(String pluginMessage) {
        this.pluginMessage = pluginMessage;
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.canceled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
