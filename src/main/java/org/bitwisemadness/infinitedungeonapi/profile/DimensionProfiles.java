package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The dimension profile registry: code registrations plus the datapack table.
 *
 * <p>A query maps a dimension id to its profile. No profile means "not a
 * dungeon dimension", and the suite's systems disengage. The two sources are
 * kept apart so a datapack reload never drops a code registration, and a
 * datapack entry wins over a code entry for the same dimension, matching the
 * suite's datapack-first rule.</p>
 *
 * <p>The class holds no Minecraft or Mojang types, so the whole registry is
 * unit-testable; the datapack side is fed by
 * {@code DimensionProfileReloadListener}.</p>
 */
public final class DimensionProfiles {

    private static final Map<String, DimensionProfile> CODE = new ConcurrentHashMap<>();

    private static volatile Map<String, DimensionProfile> datapack = Map.of();

    private DimensionProfiles() {
    }

    /** Registers a code profile; replaces any earlier one for the dimension. */
    public static void register(DimensionProfile profile) {
        Objects.requireNonNull(profile, "profile");
        CODE.put(profile.dimension(), profile.sanitised());
    }

    /** Removes a code registration. */
    public static void remove(String dimensionId) {
        if (dimensionId != null) {
            CODE.remove(dimensionId);
        }
    }

    /** Replaces the datapack table, for example on reload. */
    public static void setDatapack(Map<String, DimensionProfile> byDimension) {
        Map<String, DimensionProfile> copy = new TreeMap<>();
        if (byDimension != null) {
            byDimension.forEach((dimension, profile) -> {
                if (dimension != null && profile != null) {
                    copy.put(dimension, profile.sanitised());
                }
            });
        }
        datapack = Collections.unmodifiableMap(copy);
    }

    /** Drops the datapack table, leaving code registrations in place. */
    public static void clearDatapack() {
        datapack = Map.of();
    }

    /** The profile for a dimension, when one is registered. */
    public static Optional<DimensionProfile> get(String dimensionId) {
        if (dimensionId == null) {
            return Optional.empty();
        }
        DimensionProfile fromData = datapack.get(dimensionId);
        return fromData != null ? Optional.of(fromData)
                : Optional.ofNullable(CODE.get(dimensionId));
    }

    /** Whether a dimension is a dungeon dimension. */
    public static boolean isDungeonDimension(String dimensionId) {
        return get(dimensionId).isPresent();
    }

    /**
     * Whether a dimension is exempt from the overworld safety gates.
     *
     * <p>A profile that omits the flag is exempt (the dungeon default); a
     * dimension with no profile at all is not a dungeon dimension, so this
     * answers {@code false}.</p>
     */
    public static boolean spawnSafetyExempt(String dimensionId) {
        return get(dimensionId)
                .map(profile -> profile.spawnSafetyExempt().orElse(true))
                .orElse(false);
    }

    /** Every known profile, datapack entries winning, sorted by dimension id. */
    public static Map<String, DimensionProfile> all() {
        Map<String, DimensionProfile> merged = new TreeMap<>(CODE);
        merged.putAll(datapack);
        return Collections.unmodifiableMap(merged);
    }

    /** Clears both sources; used by reload teardown and tests. */
    public static void reset() {
        CODE.clear();
        datapack = Map.of();
    }
}
