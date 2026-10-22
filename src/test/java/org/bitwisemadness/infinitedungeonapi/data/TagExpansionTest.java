package org.bitwisemadness.infinitedungeonapi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagExpansionTest {

    private static final Set<String> EXISTING =
            Set.of("minecraft:zombie", "minecraft:skeleton", "tensura:orc");
    private static final Map<String, List<String>> TAGS = Map.of(
            "minecraft:raiders", List.of("minecraft:zombie", "tensura:absent"),
            "minecraft:empty", List.of());

    @Test
    void plainIdResolvesWhenPresent() {
        assertEquals(List.of("minecraft:zombie"),
                TagExpansion.expand(ResourceRef.parse("minecraft:zombie"), EXISTING::contains, TAGS::get));
    }

    @Test
    void plainIdIsDroppedWhenAbsent() {
        assertEquals(List.of(),
                TagExpansion.expand(ResourceRef.parse("minecraft:warden"), EXISTING::contains, TAGS::get));
    }

    @Test
    void tagExpandsToPresentMembersOnly() {
        assertEquals(List.of("minecraft:zombie"),
                TagExpansion.expand(ResourceRef.parse("#minecraft:raiders"), EXISTING::contains, TAGS::get));
    }

    @Test
    void emptyAndUnknownTagsAreTolerated() {
        assertEquals(List.of(),
                TagExpansion.expand(ResourceRef.parse("#minecraft:empty"), EXISTING::contains, TAGS::get));
        assertEquals(List.of(),
                TagExpansion.expand(ResourceRef.parse("#minecraft:missing"), EXISTING::contains, TAGS::get));
    }

    @Test
    void expandsMultipleReferencesInOrder() {
        List<ResourceRef> refs = List.of(
                ResourceRef.parse("tensura:orc"),
                ResourceRef.parse("minecraft:warden"),
                ResourceRef.parse("#minecraft:raiders"));

        assertEquals(List.of("tensura:orc", "minecraft:zombie"),
                TagExpansion.expandAll(refs, EXISTING::contains, TAGS::get));
    }
}
