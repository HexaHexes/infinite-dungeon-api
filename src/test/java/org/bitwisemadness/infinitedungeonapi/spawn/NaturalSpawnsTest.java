package org.bitwisemadness.infinitedungeonapi.spawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class NaturalSpawnsTest {

    @Test
    void naturalSpawnsAreInScope() {
        assertTrue(NaturalSpawns.allows("NATURAL"));
        assertTrue(NaturalSpawns.allows(NaturalSpawns.NATURAL));
    }

    @Test
    void everyOtherSpawnTypeStaysUngated() {
        for (String type : Set.of("SPAWNER", "STRUCTURE", "COMMAND", "EVENT", "PATROL",
                "TRIGGERED", "BUCKET", "SPAWN_EGG", "MOB_SUMMONED", "DISPENSER", "BREEDING")) {
            assertFalse(NaturalSpawns.allows(type), type + " must never be gated");
        }
    }

    @Test
    void unknownOrMissingNamesDefaultToUngated() {
        assertFalse(NaturalSpawns.allows(null));
        assertFalse(NaturalSpawns.allows(""));
        assertFalse(NaturalSpawns.allows("A_TYPE_FROM_A_FUTURE_VERSION"));
    }

    @Test
    void allowlistContainsOnlyNatural() {
        assertEquals(Set.of("NATURAL"), NaturalSpawns.allowedNames());
    }
}
