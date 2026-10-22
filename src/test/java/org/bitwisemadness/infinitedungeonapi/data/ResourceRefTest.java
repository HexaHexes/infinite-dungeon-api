package org.bitwisemadness.infinitedungeonapi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ResourceRefTest {

    @Test
    void parsesPlainId() {
        ResourceRef ref = ResourceRef.parse("minecraft:zombie");
        assertEquals("minecraft:zombie", ref.location());
        assertFalse(ref.tag());
    }

    @Test
    void parsesUnqualifiedId() {
        ResourceRef ref = ResourceRef.parse("zombie");
        assertEquals("zombie", ref.location());
        assertFalse(ref.tag());
    }

    @Test
    void parsesTag() {
        ResourceRef ref = ResourceRef.parse("#minecraft:is_forest");
        assertEquals("minecraft:is_forest", ref.location());
        assertTrue(ref.tag());
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertEquals(ResourceRef.parse("#minecraft:is_forest"),
                ResourceRef.parse("  #minecraft:is_forest  "));
    }

    @Test
    void roundTripsThroughSerialization() {
        for (String raw : new String[] {"minecraft:zombie", "#minecraft:is_forest", "zombie"}) {
            assertEquals(raw, ResourceRef.parse(raw).serialize());
        }
    }

    @Test
    void rejectsMalformedReferences() {
        assertEquals(Optional.empty(), ResourceRef.tryParse(null));
        assertEquals(Optional.empty(), ResourceRef.tryParse(""));
        assertEquals(Optional.empty(), ResourceRef.tryParse("#"));
        assertEquals(Optional.empty(), ResourceRef.tryParse(":zombie"));
        assertEquals(Optional.empty(), ResourceRef.tryParse("minecraft:"));
        assertEquals(Optional.empty(), ResourceRef.tryParse("minecraft:zombie#tail"));
        assertEquals(Optional.empty(), ResourceRef.tryParse("Minecraft:Zombie"));
        assertEquals(Optional.empty(), ResourceRef.tryParse("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("Minecraft:Zombie"));
    }
}
