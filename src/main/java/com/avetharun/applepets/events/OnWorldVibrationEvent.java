package com.avetharun.applepets.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a vibration is emitted
 * <p>
 * If the event is cancelled, the vibration is removed.
 */
public class OnWorldVibrationEvent extends Event implements Cancellable {
    private Location origin;
    @Nullable
    private Entity source;
    private boolean isCancelled;

    public OnWorldVibrationEvent(Location location, String spell) {
        this.origin = location;
        this.isCancelled = false;
    }

    public Location getOrigin() {
        return this.origin;
    }
    // Returns the source of the vibration, if any
    @Nullable
    public Entity getSource() {
        return source;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

}
