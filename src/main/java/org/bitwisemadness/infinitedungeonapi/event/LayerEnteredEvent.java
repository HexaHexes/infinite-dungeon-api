package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A player crossed into a new layer.
 *
 * <p>Carries the player's id rather than the player, so a long-lived listener
 * cannot retain an entity and a consumer that needs the player resolves the id
 * itself. Notification only.</p>
 */
public final class LayerEnteredEvent extends DungeonEvent {

    private final UUID playerId;
    private final int layer;

    public LayerEnteredEvent(ResourceKey<Level> dimension, UUID playerId, int layer) {
        super(dimension);
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
    }

    /** The player who crossed the boundary. */
    public UUID playerId() {
        return playerId;
    }

    /** The layer index entered. */
    public int layer() {
        return layer;
    }
}
