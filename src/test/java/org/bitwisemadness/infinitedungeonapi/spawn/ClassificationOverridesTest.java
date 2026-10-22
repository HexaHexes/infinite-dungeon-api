package org.bitwisemadness.infinitedungeonapi.spawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClassificationOverridesTest {

    @AfterEach
    void tearDown() {
        ClassificationOverrides.clear();
    }

    @Test
    void startsEmpty() {
        assertEquals(Map.of(), ClassificationOverrides.all());
        assertEquals(Optional.empty(), ClassificationOverrides.get("minecraft:zombie"));
    }

    @Test
    void storesAndLooksUpOverrides() {
        ClassificationOverrides.set(Map.of(
                "minecraft:zombie", SpawnCategory.NEUTRAL,
                "minecraft:cow", SpawnCategory.HOSTILE));

        assertEquals(Optional.of(SpawnCategory.NEUTRAL), ClassificationOverrides.get("minecraft:zombie"));
        assertEquals(Optional.of(SpawnCategory.HOSTILE), ClassificationOverrides.get("minecraft:cow"));
        assertEquals(Optional.empty(), ClassificationOverrides.get("minecraft:warden"));
    }

    @Test
    void exposesASortedUnmodifiableView() {
        ClassificationOverrides.set(Map.of(
                "z:last", SpawnCategory.PASSIVE,
                "a:first", SpawnCategory.HOSTILE));

        assertEquals(java.util.List.of("a:first", "z:last"),
                java.util.List.copyOf(ClassificationOverrides.all().keySet()));
        assertThrows(UnsupportedOperationException.class,
                () -> ClassificationOverrides.all().put("b:mid", SpawnCategory.NEUTRAL));
    }

    @Test
    void clearEmptiesTheTable() {
        ClassificationOverrides.set(Map.of("minecraft:zombie", SpawnCategory.NEUTRAL));
        assertTrue(ClassificationOverrides.get("minecraft:zombie").isPresent());
        ClassificationOverrides.clear();
        assertEquals(Optional.empty(), ClassificationOverrides.get("minecraft:zombie"));
    }
}
