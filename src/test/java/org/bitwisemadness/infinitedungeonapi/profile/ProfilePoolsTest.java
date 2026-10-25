package org.bitwisemadness.infinitedungeonapi.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Pool ids: normalisation, stable order, and malformed-reference handling. */
class ProfilePoolsTest {

    @Test
    void blankIdsAreTreatedAsAbsent() {
        ProfilePools pools = new ProfilePools(Optional.of("   "), Optional.of(""), Optional.empty());
        assertTrue(pools.ids().isEmpty(), "blank ids must not count as present");
    }

    @Test
    void idsKeepTheirFieldOrder() {
        ProfilePools pools = new ProfilePools(
                Optional.of("core:fodder"), Optional.of("core:elite"), Optional.of("core:boss"));
        assertEquals(List.of("core:fodder", "core:elite", "core:boss"), pools.ids());
    }

    @Test
    void malformedReferencesAreReported() {
        ProfilePools pools = new ProfilePools(
                Optional.of("core:fodder"),
                Optional.of("Not A Location!"),
                Optional.of("#core:tag"));
        // A tag reference is not a pool id either, so it is malformed too.
        assertEquals(List.of("Not A Location!", "#core:tag"), pools.malformedIds());
    }

    @Test
    void sanitisedDropsOnlyMalformedIds() {
        ProfilePools pools = new ProfilePools(
                Optional.of("core:fodder"), Optional.of("BAD!"), Optional.of("core:boss"));
        ProfilePools clean = pools.sanitised();
        assertEquals(Optional.of("core:fodder"), clean.fodder());
        assertTrue(clean.elite().isEmpty());
        assertEquals(Optional.of("core:boss"), clean.boss());
        assertTrue(clean.malformedIds().isEmpty());
    }

    @Test
    void emptyNamesNothing() {
        assertTrue(ProfilePools.empty().ids().isEmpty());
        assertTrue(ProfilePools.empty().malformedIds().isEmpty());
    }
}
