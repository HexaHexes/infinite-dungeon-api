package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The entrance location-provider registry (api#58). Providers register under
 * a key - an entrance's identity - and the generation policy asks the
 * registered provider for each entrance. The class holds no Mojang types, so
 * the registry is unit-testable; the providers themselves are server-side.
 */
public final class EntranceLocations {

    private static final Map<String, EntranceLocationProvider> PROVIDERS = new HashMap<>();

    private EntranceLocations() {
    }

    /** Registers the provider for an entrance key; replaces any earlier one. */
    public static void register(String key, EntranceLocationProvider provider) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(provider, "provider");
        PROVIDERS.put(key, provider);
    }

    /** Removes the registration for a key. */
    public static void remove(String key) {
        if (key != null) {
            PROVIDERS.remove(key);
        }
    }

    /** The registered provider for the key, or empty when none is registered. */
    public static Optional<EntranceLocationProvider> get(String key) {
        return Optional.ofNullable(PROVIDERS.get(key));
    }

    /** Whether any provider is registered (test hygiene). */
    public static void clear() {
        PROVIDERS.clear();
    }
}
