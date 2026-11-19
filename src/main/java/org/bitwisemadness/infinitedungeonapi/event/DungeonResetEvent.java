package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A timed reset of a cache-like dungeon state: the loot or mob resets fired
 * by the timed-reset feature (core #77). Notification only; carries the kind
 * and the layer scope, with no player attribution - a timer reset is not
 * player-driven.
 */
public final class DungeonResetEvent extends DungeonEvent {

    /** What was reset: the loot containers or the mob population. */
    public enum Kind { LOOT, MOB }

    private final Kind kind;
    private final int layer;

    public DungeonResetEvent(ResourceKey<Level> dimension, Kind kind, int layer) {
        super(Objects.requireNonNull(dimension, "dimension"));
        this.kind = Objects.requireNonNull(kind, "kind");
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
    }

    /** What the timed reset refreshed. */
    public Kind kind() {
        return kind;
    }

    /** The layer whose state was reset. */
    public int layer() {
        return layer;
    }
}
