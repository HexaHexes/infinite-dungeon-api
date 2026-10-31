package org.bitwisemadness.infinitedungeonapi.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The stream contract's name validation (issue api#84), which is all this
 * source set can pin: Minecraft's RandomSource stays off the unit-test
 * classpath (the same rule that puts the codec round-trip in the gametest
 * suite), so the value pins - identical inputs, independent names, no
 * cross-decision shifting - live in {@code ApiSmokeGameTests}.
 */
class DeterminismTest {

    private static final long SEED = 1234L;

    @Test
    void aMissingDecisionNameNamesTheOffendingCase() {
        var empty = assertThrows(IllegalArgumentException.class,
                () -> Determinism.nextLong("", SEED, 600, -64, 900));
        assertTrue(empty.getMessage().contains("non-empty decision name"),
                empty.getMessage());
        var nullName = assertThrows(IllegalArgumentException.class,
                () -> Determinism.nextLong(null, SEED, 600, -64, 900));
        assertTrue(nullName.getMessage().contains("non-empty decision name"),
                nullName.getMessage());
    }
}
