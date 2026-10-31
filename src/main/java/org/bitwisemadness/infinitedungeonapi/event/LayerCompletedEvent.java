package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A layer's guardian fell for the first time, so the layer counts as completed.
 *
 * <p>Fired once per layer per world; the guardian's later respawns do not fire
 * it again. Notification only.</p>
 *
 * <p>Attribution follows ADR-0003: the completer is the player attributable to
 * the fall that first unlocked the layer - typically its killer - and is
 * <em>absent</em> when that fall was environmental or no player was around.
 * Unknown is documented state, never a guess: presence (who happened to be in
 * the layer) is not causation and must not be used to fill the gap.</p>
 */
public final class LayerCompletedEvent extends DungeonEvent {

    private final int layer;
    /** Nullable: absent is the documented unknown (ADR-0003). */
    private final UUID completingPlayer;

    public LayerCompletedEvent(ResourceKey<Level> dimension, int layer) {
        this(dimension, layer, null);
    }

    /**
     * @param completingPlayer the completing player's id, or {@code null} when
     *     no completion is player-attributable - both read back as an empty
     *     {@link #completingPlayer()}, ADR-0003's documented unknown
     */
    public LayerCompletedEvent(ResourceKey<Level> dimension, int layer, UUID completingPlayer) {
        super(Objects.requireNonNull(dimension, "dimension"));
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
        this.completingPlayer = completingPlayer;
    }

    /** The completed layer index. */
    public int layer() {
        return layer;
    }

    /**
     * The completing player's id where the completion is attributable to
     * one; empty for zero-player completions and for events built without an
     * attribution argument (ADR-0003's documented unknown).
     */
    public Optional<UUID> completingPlayer() {
        return Optional.ofNullable(completingPlayer);
    }
}
