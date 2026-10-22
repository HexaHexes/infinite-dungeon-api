package org.bitwisemadness.infinitedungeonapi.spawn;

import java.util.Set;

/**
 * The documented scope of magicule spawn gating.
 *
 * <p>Only post-chunk-generation natural spawns are in scope. Spawner,
 * structure, command, egg, event, and patrol spawns are never blocked or
 * allowed by magicule concentration, and chunk generation itself is untouched.
 * This is an allowlist: anything not named here stays ungated, so a new spawn
 * type in a future Minecraft version defaults to ungated rather than silently
 * gaining the gate.</p>
 *
 * <p>Names are plain strings so the allowlist is unit-testable without a game
 * runtime; game-facing code passes {@code MobSpawnType.name()}.</p>
 */
public final class NaturalSpawns {

    /** The only gated spawn type. */
    public static final String NATURAL = "NATURAL";

    private static final Set<String> ALLOWED = Set.of(NATURAL);

    private NaturalSpawns() {
    }

    /** Whether a spawn of this type is in scope for magicule gating. */
    public static boolean allows(String spawnTypeName) {
        return spawnTypeName != null && ALLOWED.contains(spawnTypeName);
    }

    /** The gated spawn type names. */
    public static Set<String> allowedNames() {
        return ALLOWED;
    }
}
