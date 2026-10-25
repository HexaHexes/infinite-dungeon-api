package org.bitwisemadness.infinitedungeonapi.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A layer's guardian fell for the first time, so the layer counts as completed.
 *
 * <p>Fired once per layer per world; the guardian's later respawns do not fire
 * it again. Notification only.</p>
 */
public final class LayerCompletedEvent extends DungeonEvent {

    private final int layer;

    public LayerCompletedEvent(ResourceKey<Level> dimension, int layer) {
        super(dimension);
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
    }

    /** The completed layer index. */
    public int layer() {
        return layer;
    }
}
