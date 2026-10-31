package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * A floor miniboss fell.
 *
 * <p>Posted by the core module once per miniboss fall, after the floor's
 * miniboss stamp - so a consumer can credit the raid without polling saved
 * data or scanning entities. Notification only, server-side, with no ordering
 * promise relative to the other lifecycle events (see {@link DungeonEvent}).</p>
 *
 * <p>The core's floor tag carries layer and floor authoritatively, so both are
 * exact rather than re-derived from position. The boss rides valid-at-fire,
 * like {@link BossSlainEvent}'s. Killing-player attribution follows ADR-0003
 * from this event's birth: present where attributable, empty for environmental
 * or mob kills - documented unknown, never a guess.</p>
 */
public final class MinibossSlainEvent extends DungeonEvent {

    private final int layer;
    private final int floor;
    private final LivingEntity boss;
    /** Nullable: absent is the documented unknown (ADR-0003). */
    private final UUID killingPlayer;

    /**
     * @param killingPlayer the killing player's id, or {@code null} when the
     *     kill is not player-attributable - both read back as an empty
     *     {@link #killingPlayer()}, ADR-0003's documented unknown
     */
    public MinibossSlainEvent(ResourceKey<Level> dimension, int layer, int floor, LivingEntity boss,
            UUID killingPlayer) {
        super(Objects.requireNonNull(dimension, "dimension"));
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        if (floor < 0) {
            throw new IllegalArgumentException("floor must not be negative");
        }
        this.layer = layer;
        this.floor = floor;
        this.boss = Objects.requireNonNull(boss, "boss");
        this.killingPlayer = killingPlayer;
    }

    /** The layer the miniboss floor belongs to. */
    public int layer() {
        return layer;
    }

    /** The floor index within the layer. */
    public int floor() {
        return floor;
    }

    /** The miniboss entity, valid while the event is fired. */
    public LivingEntity boss() {
        return boss;
    }

    /**
     * The killing player's id where the kill is attributable to one;
     * empty for non-player kills (ADR-0003's documented unknown).
     */
    public Optional<UUID> killingPlayer() {
        return Optional.ofNullable(killingPlayer);
    }
}
