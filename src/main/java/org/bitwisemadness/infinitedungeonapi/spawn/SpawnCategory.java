package org.bitwisemadness.infinitedungeonapi.spawn;

/**
 * How the suite treats a mob for spawn control.
 *
 * <p>Deliberately not named {@code MobCategory}: that name belongs to vanilla,
 * and the two are different axes. This is the derived, safety-facing category;
 * the vanilla category is one input to deriving it.</p>
 */
public enum SpawnCategory {

    /** Attacking mobs: blocked by default in safe dimensions. */
    HOSTILE,

    /** Mobs that only fight back, or fight conditionally. */
    NEUTRAL,

    /** Non-fighting mobs. */
    PASSIVE
}
