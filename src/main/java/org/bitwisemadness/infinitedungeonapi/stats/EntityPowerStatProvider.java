package org.bitwisemadness.infinitedungeonapi.stats;

import java.util.OptionalDouble;
import net.minecraft.world.entity.LivingEntity;

/**
 * A source of entity power values.
 *
 * <p>Providers answer for a living entity, or return empty when they cannot
 * speak for that entity or stat. Implementations must be server-side, must not
 * block, and must be safe to call during ticking and spawn finalization.</p>
 */
@FunctionalInterface
public interface EntityPowerStatProvider {

    /** The value of the stat for the entity, or empty when this provider has none. */
    OptionalDouble of(LivingEntity entity, StatKind kind);
}
