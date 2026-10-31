package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * A layer boss died.
 *
 * <p>Carries the boss because consumers (checklists, quest hooks, loot hooks)
 * need to know which one; it is fired while the entity is still valid.
 * Notification only.</p>
 *
 * <p>Attribution follows ADR-0003: where the kill is attributable to a
 * player, the id is carried; for environmental or mob kills - and for events
 * built through the legacy constructor - it is <em>absent</em>. Unknown is
 * documented state, never a guess: a listener must not correlate presence to
 * invent a killer.</p>
 */
public final class BossSlainEvent extends DungeonEvent {

    private final int layer;
    private final LivingEntity boss;
    /** Nullable: absent is the documented unknown (ADR-0003). */
    private final UUID killingPlayer;

    public BossSlainEvent(ResourceKey<Level> dimension, int layer, LivingEntity boss) {
        this(dimension, layer, boss, null);
    }

    /**
     * @param killingPlayer the killing player's id, or {@code null} when the
     *     kill is not player-attributable - both read back as an empty
     *     {@link #killingPlayer()}, ADR-0003's documented unknown
     */
    public BossSlainEvent(ResourceKey<Level> dimension, int layer, LivingEntity boss, UUID killingPlayer) {
        super(Objects.requireNonNull(dimension, "dimension"));
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
        this.boss = Objects.requireNonNull(boss, "boss");
        this.killingPlayer = killingPlayer;
    }

    /** The layer whose boss died. */
    public int layer() {
        return layer;
    }

    /** The boss entity. */
    public LivingEntity boss() {
        return boss;
    }

    /**
     * The killing player's id where the kill is attributable to one;
     * empty for non-player kills and for events built without an
     * attribution argument (ADR-0003's documented unknown).
     */
    public Optional<UUID> killingPlayer() {
        return Optional.ofNullable(killingPlayer);
    }
}
