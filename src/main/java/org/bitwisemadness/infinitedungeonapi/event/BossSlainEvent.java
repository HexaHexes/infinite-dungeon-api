package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * A layer boss died.
 *
 * <p>Carries the boss because consumers (checklists, quest hooks, loot hooks)
 * need to know which one; it is fired while the entity is still valid.
 * Notification only.</p>
 */
public final class BossSlainEvent extends DungeonEvent {

    private final int layer;
    private final LivingEntity boss;

    public BossSlainEvent(ResourceKey<Level> dimension, int layer, LivingEntity boss) {
        super(Objects.requireNonNull(dimension, "dimension"));
        if (layer < 0) {
            throw new IllegalArgumentException("layer must not be negative");
        }
        this.layer = layer;
        this.boss = Objects.requireNonNull(boss, "boss");
    }

    /** The layer whose boss died. */
    public int layer() {
        return layer;
    }

    /** The boss entity. */
    public LivingEntity boss() {
        return boss;
    }
}
