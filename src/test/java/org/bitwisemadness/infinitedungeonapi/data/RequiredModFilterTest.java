package org.bitwisemadness.infinitedungeonapi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequiredModFilterTest {

    private static final java.util.function.Predicate<String> PRESENT =
            Set.of("tensura", "iceandfire")::contains;

    @Test
    void entriesWithoutRequirementAreAlwaysAllowed() {
        assertTrue(RequiredModFilter.allowed(Optional.empty(), PRESENT));
        assertTrue(RequiredModFilter.allowed(null, PRESENT));
        assertTrue(RequiredModFilter.allowed(Optional.of("  "), PRESENT));
    }

    @Test
    void presentModsAreAllowedAndAbsentOnesFiltered() {
        assertTrue(RequiredModFilter.allowed(Optional.of("tensura"), PRESENT));
        assertFalse(RequiredModFilter.allowed(Optional.of("cataclysm"), PRESENT));
    }

    @Test
    void appliesToEntryListsPreservingOrder() {
        record Entry(String name, Optional<String> requiredMod) {
        }
        List<Entry> entries = List.of(
                new Entry("always", Optional.empty()),
                new Entry("tensura", Optional.of("tensura")),
                new Entry("cataclysm", Optional.of("cataclysm")),
                new Entry("iceandfire", Optional.of("iceandfire")));

        List<String> kept = RequiredModFilter
                .filter(entries, Entry::requiredMod, PRESENT)
                .stream().map(Entry::name).toList();

        assertEquals(List.of("always", "tensura", "iceandfire"), kept);
    }
}
