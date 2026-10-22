package org.bitwisemadness.infinitedungeonapi.data;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The suite's {@code required_mod} gate.
 *
 * <p>An entry naming an absent mod is skipped silently rather than failing the
 * reload, so a pack can ship data referencing optional content. Mod presence is
 * injected as a predicate, which keeps this logic unit-testable without a
 * runtime and lets callers supply FML's mod list at the boundary.</p>
 */
public final class RequiredModFilter {

    private RequiredModFilter() {
    }

    /** True when no mod is required, or the required mod is present. */
    public static boolean allowed(Optional<String> requiredMod, Predicate<String> modPresent) {
        if (requiredMod == null || requiredMod.isEmpty()) {
            return true;
        }
        String modId = requiredMod.get();
        if (modId == null || modId.isBlank()) {
            return true;
        }
        return modPresent.test(modId);
    }

    /** Filters entries by their required-mod field, preserving input order. */
    public static <T> List<T> filter(List<T> entries,
                                     Function<T, Optional<String>> requiredModOf,
                                     Predicate<String> modPresent) {
        return entries.stream()
                .filter(entry -> allowed(requiredModOf.apply(entry), modPresent))
                .toList();
    }
}
