package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The depth API reported a new floor within a layer.
 *
 * <p>Carries the player's id rather than the player, so a long-lived listener
 * cannot retain an entity. Notification only.</p>
 */
public final class FloorEnteredEvent extends DungeonEvent {

    private final UUID playerId;
    private final int layer;
    private final int floor;

    public FloorEnteredEvent(ResourceKey<Level> dimension, UUID playerId, int layer, int floor) {
        super(Objects.requireNonNull(dimension, "dimension"));
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        if (floor < 0) {
            throw new IllegalArgumentException("floor must not be negative");
        }
        this.layer = layer;
        this.floor = floor;
    }

    /** The player who reached the floor. */
    public UUID playerId() {
        return playerId;
    }

    /** The layer the floor belongs to. */
    public int layer() {
        return layer;
    }

    /** The floor index within the layer. */
    public int floor() {
        return floor;
    }
}
