package org.bitwisemadness.infinitedungeonapi.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The registry: code plus datapack, datapack winning, reload-safe. */
class DimensionProfilesTest {

    @AfterEach
    void tearDown() {
        DimensionProfiles.reset();
    }

    private static DimensionProfile profile(String dimension, Integer layerLimit, Boolean exempt) {
        return new DimensionProfile(dimension,
                Optional.ofNullable(layerLimit),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.ofNullable(exempt));
    }

    @Test
    void codeRegistrationResolvesADimension() {
        DimensionProfiles.register(DimensionProfile.forDimension("infinite_dungeon:dungeon"));
        assertTrue(DimensionProfiles.isDungeonDimension("infinite_dungeon:dungeon"));
        assertFalse(DimensionProfiles.isDungeonDimension("minecraft:overworld"));
    }

    @Test
    void removeDropsACodeRegistration() {
        DimensionProfiles.register(DimensionProfile.forDimension("infinite_dungeon:dungeon"));
        DimensionProfiles.remove("infinite_dungeon:dungeon");
        assertFalse(DimensionProfiles.isDungeonDimension("infinite_dungeon:dungeon"));
    }

    @Test
    void datapackEntriesWinOverCode() {
        DimensionProfiles.register(profile("infinite_dungeon:dungeon", 3, null));
        DimensionProfiles.setDatapack(Map.of("infinite_dungeon:dungeon", profile("infinite_dungeon:dungeon", 9, null)));
        assertEquals(Optional.of(9),
                DimensionProfiles.get("infinite_dungeon:dungeon").orElseThrow().layerLimit());
    }

    @Test
    void reloadKeepsCodeRegistrations() {
        DimensionProfiles.register(DimensionProfile.forDimension("core:code"));
        DimensionProfiles.setDatapack(Map.of("core:data", DimensionProfile.forDimension("core:data")));
        DimensionProfiles.setDatapack(Map.of());
        assertTrue(DimensionProfiles.get("core:code").isPresent());
        assertTrue(DimensionProfiles.get("core:data").isEmpty());
    }

    @Test
    void spawnSafetyExemptionDefaultsToTrueForADungeon() {
        DimensionProfiles.register(DimensionProfile.forDimension("infinite_dungeon:dungeon"));
        assertTrue(DimensionProfiles.spawnSafetyExempt("infinite_dungeon:dungeon"),
                "a profile that omits the flag is exempt");
        DimensionProfiles.register(profile("infinite_dungeon:dungeon", null, false));
        assertFalse(DimensionProfiles.spawnSafetyExempt("infinite_dungeon:dungeon"));
        assertFalse(DimensionProfiles.spawnSafetyExempt("minecraft:overworld"),
                "a dimension with no profile is not a dungeon dimension");
    }

    @Test
    void allMergesBothSourcesSorted() {
        DimensionProfiles.register(DimensionProfile.forDimension("z:code"));
        DimensionProfiles.setDatapack(Map.of("a:data", DimensionProfile.forDimension("a:data")));
        assertEquals(List.of("a:data", "z:code"), List.copyOf(DimensionProfiles.all().keySet()));
    }

    @Test
    void datapackSetSanitisesPoolIds() {
        DimensionProfile dirty = new DimensionProfile("infinite_dungeon:dungeon",
                Optional.empty(), Optional.empty(),
                Optional.of(new ProfilePools(Optional.of("BAD!"), Optional.empty(), Optional.empty())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        DimensionProfiles.setDatapack(Map.of("infinite_dungeon:dungeon", dirty));
        assertTrue(DimensionProfiles.get("infinite_dungeon:dungeon").orElseThrow()
                .malformedPoolIds().isEmpty());
    }
}
