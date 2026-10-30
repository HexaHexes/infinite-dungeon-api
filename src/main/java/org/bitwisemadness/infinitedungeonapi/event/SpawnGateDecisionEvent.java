package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import org.bitwisemadness.infinitedungeonapi.spawn.SpawnCategory;

/**
 * The safety module is about to block a natural spawn.
 *
 * <p><strong>The one cancellable lifecycle event.</strong> Fired just before the
 * gate rejects a spawn; cancelling it overrides the block, which is how a pack
 * or integration re-allows a spawn the gate would otherwise refuse. The event
 * carries the facts the decision used - the mob, its category, the spawn
 * position, and the ambient and required magicule - so a listener can judge the
 * override itself. The safety module's own reason code stays in that module and
 * is not part of this contract.</p>
 *
 * <p>Fired by the spawn-safety module, not the core module. Unlike the other
 * lifecycle events, a listener is expected to inspect and possibly veto it.</p>
 */
public final class SpawnGateDecisionEvent extends DungeonEvent implements ICancellableEvent {

    private final Mob mob;
    private final SpawnCategory category;
    private final BlockPos position;
    private final double ambient;
    private final double required;

    public SpawnGateDecisionEvent(ResourceKey<Level> dimension,
                                  Mob mob,
                                  SpawnCategory category,
                                  BlockPos position,
                                  double ambient,
                                  double required) {
        super(Objects.requireNonNull(dimension, "dimension"));
        this.mob = Objects.requireNonNull(mob, "mob");
        this.category = Objects.requireNonNull(category, "category");
        this.position = Objects.requireNonNull(position, "position").immutable();
        this.ambient = ambient;
        this.required = required;
    }

    /** The mob the gate is about to reject. */
    public Mob mob() {
        return mob;
    }

    /** The mob's classification category. */
    public SpawnCategory category() {
        return category;
    }

    /** Where the spawn would have happened. */
    public BlockPos position() {
        return position;
    }

    /** The ambient magicule at the spawn position. */
    public double ambient() {
        return ambient;
    }

    /** The ambient magicule the mob needed. */
    public double required() {
        return required;
    }
}
