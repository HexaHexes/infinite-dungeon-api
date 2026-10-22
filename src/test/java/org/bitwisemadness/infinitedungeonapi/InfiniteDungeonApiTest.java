package org.bitwisemadness.infinitedungeonapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Placeholder JUnit suite. The CI {@code junit} check asserts a non-zero test
 * count so it cannot pass vacuously, so this file stays and grows real unit
 * tests as the API surface lands.
 */
class InfiniteDungeonApiTest {

    @Test
    void modIdIsALowercasePathSegment() {
        // MOD_ID is a compile-time constant, so referencing it does not trigger
        // class initialization - this test needs no Minecraft runtime.
        assertTrue(InfiniteDungeonApi.MOD_ID.matches("[a-z][a-z0-9_]{2,63}"),
                "mod id must be a lowercase resource location path segment");
    }
}
