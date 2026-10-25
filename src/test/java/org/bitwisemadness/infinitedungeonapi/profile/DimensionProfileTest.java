package org.bitwisemadness.infinitedungeonapi.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The profile declaration: required dimension, id validation, sanitising. */
class DimensionProfileTest {

    @Test
    void dimensionIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> DimensionProfile.forDimension(" "));
        assertThrows(IllegalArgumentException.class, () -> DimensionProfile.forDimension(null));
    }

    @Test
    void wellFormedDimensionIsRecognised() {
        assertTrue(DimensionProfile.forDimension("infinite_dungeon:dungeon").hasWellFormedDimension());
        assertFalse(DimensionProfile.forDimension("Not A Dimension").hasWellFormedDimension());
    }

    @Test
    void absentOptionalsAreTolerated() {
        DimensionProfile profile = new DimensionProfile("infinite_dungeon:dungeon",
                null, null, null, null, null, null, null, null);
        assertTrue(profile.layerLimit().isEmpty());
        assertTrue(profile.spawnSafetyExempt().isEmpty());
        assertTrue(profile.malformedPoolIds().isEmpty());
    }

    @Test
    void sanitisedLeavesACleanProfileAlone() {
        DimensionProfile profile = DimensionProfile.forDimension("infinite_dungeon:dungeon");
        assertSame(profile, profile.sanitised());
    }

    @Test
    void malformedPoolIdsAreListedAndDropped() {
        DimensionProfile profile = new DimensionProfile("infinite_dungeon:dungeon",
                Optional.empty(), Optional.empty(),
                Optional.of(new ProfilePools(Optional.of("core:fodder"), Optional.of("BAD!"), Optional.empty())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        assertEquals(List.of("BAD!"), profile.malformedPoolIds());
        assertTrue(profile.sanitised().malformedPoolIds().isEmpty());
        assertEquals(Optional.of("core:fodder"), profile.sanitised().pools().orElseThrow().fodder());
    }
}
