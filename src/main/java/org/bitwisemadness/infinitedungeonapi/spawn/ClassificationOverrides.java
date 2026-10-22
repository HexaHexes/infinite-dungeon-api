package org.bitwisemadness.infinitedungeonapi.spawn;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Datapack-provided classification overrides, keyed by entity type id.
 *
 * <p>Override-only: no curated lists ship. A pack that disagrees with the
 * derived chain names the entity here and its entry wins. Keys are plain
 * strings so this stays Minecraft-free and unit-testable.</p>
 */
public final class ClassificationOverrides {

    private static volatile Map<String, SpawnCategory> overrides = Map.of();

    private ClassificationOverrides() {
    }

    /** Replaces the override table, for example on datapack reload. */
    public static void set(Map<String, SpawnCategory> entries) {
        overrides = Collections.unmodifiableMap(new TreeMap<>(entries));
    }

    /** The override for an entity type id, when one exists. */
    public static Optional<SpawnCategory> get(String entityTypeId) {
        return Optional.ofNullable(overrides.get(entityTypeId));
    }

    /** The current override table, sorted by entity type id. */
    public static Map<String, SpawnCategory> all() {
        return overrides;
    }

    /** Clears the table; used by reload teardown and tests. */
    public static void clear() {
        overrides = Map.of();
    }
}
