package org.bitwisemadness.infinitedungeonapi.magicule;

import java.util.OptionalDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * A source of ambient magicule concentration.
 *
 * <p>Providers answer for a position, or return empty when the position is not
 * theirs to answer for. Implementations must be server-side, must not block,
 * and must be safe to call during ticking.</p>
 */
@FunctionalInterface
public interface MagiculeProvider {

    /** The concentration at the position, or empty when this provider has no answer. */
    OptionalDouble at(Level level, BlockPos pos);
}
