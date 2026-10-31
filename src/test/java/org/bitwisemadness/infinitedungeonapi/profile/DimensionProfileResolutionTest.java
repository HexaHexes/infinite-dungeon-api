package org.bitwisemadness.infinitedungeonapi.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Precedence: config defaults first, profile second - a declared value wins, an
 * absent one falls through.
 */
class DimensionProfileResolutionTest {

    private static DimensionProfileSettings defaults() {
        return new DimensionProfileSettings(
                Optional.of(12),
                new FloorsPerLayer(5, 10),
                new ProfilePools(
                        Optional.of("core:default_fodder"),
                        Optional.of("core:default_elite"),
                        Optional.of("core:default_boss")),
                new CurveOverrides(
                        Optional.of(new CurveCoefficients(0.0, 0.15, 0.05)),
                        Optional.of(new CurveCoefficients(0.0, 0.1, 0.04)),
                        Optional.of(new CurveCoefficients(0.0, 0.1, 0.03))),
                new GuardianRules(300, 10.0),
                PortalPolicy.defaults(),
                new ProfileAnnouncements("dungeon.entry", "dungeon.layer"),
                true,
                BossRespawnPolicy.defaults());
    }

    @Test
    void absentFieldsFallBackToDefaults() {
        DimensionProfileSettings resolved =
                DimensionProfile.forDimension("infinite_dungeon:dungeon").resolve(defaults());
        assertEquals(Optional.of(12), resolved.layerLimit());
        assertEquals(new FloorsPerLayer(5, 10), resolved.floorsPerLayer());
        assertEquals(new GuardianRules(300, 10.0), resolved.guardian());
        assertEquals(PortalPolicy.defaults(), resolved.portal());
        assertEquals(new ProfileAnnouncements("dungeon.entry", "dungeon.layer"), resolved.announcements());
        assertTrue(resolved.spawnSafetyExempt());
        assertEquals(Optional.of("core:default_fodder"), resolved.pools().fodder());
        assertEquals(BossRespawnPolicy.defaults(), resolved.bossRespawn(),
                "an absent respawn policy falls to the default");
    }

    @Test
    void declaredValuesWinOverDefaults() {
        DimensionProfile profile = new DimensionProfile("infinite_dungeon:dungeon",
                Optional.of(20),
                Optional.of(new FloorsPerLayer(3, 4)),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new GuardianRules(60, 25.0)),
                Optional.of(new PortalPolicy(PortalReturnMode.WORLD_SPAWN, false, false)),
                Optional.of(new ProfileAnnouncements("custom.entry", "custom.layer")),
                Optional.of(false),
                Optional.of(new BossRespawnPolicy(BossRespawnMode.TIMER, 500)));
        DimensionProfileSettings resolved = profile.resolve(defaults());
        assertEquals(Optional.of(20), resolved.layerLimit());
        assertEquals(new FloorsPerLayer(3, 4), resolved.floorsPerLayer());
        assertEquals(new GuardianRules(60, 25.0), resolved.guardian());
        assertEquals(PortalReturnMode.WORLD_SPAWN, resolved.portal().returnMode());
        assertFalse(resolved.portal().allowConstruction());
        assertFalse(resolved.portal().reformMissingPortal());
        assertEquals(new ProfileAnnouncements("custom.entry", "custom.layer"), resolved.announcements());
        assertFalse(resolved.spawnSafetyExempt());
        assertEquals(BossRespawnMode.TIMER, resolved.bossRespawn().mode(),
                "the declared respawn mode must win over the default");
        assertEquals(500, resolved.bossRespawn().respawnTicks(),
                "the declared respawn delay must win over the default");
    }

    @Test
    void poolsAndCurvesMergePerMember() {
        DimensionProfile profile = new DimensionProfile("infinite_dungeon:dungeon",
                Optional.empty(), Optional.empty(),
                Optional.of(new ProfilePools(Optional.of("pack:fodder"), Optional.empty(), Optional.empty())),
                Optional.of(new CurveOverrides(
                        Optional.of(new CurveCoefficients(1.0, 2.0, 3.0)),
                        Optional.empty(),
                        Optional.empty())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty());
        DimensionProfileSettings resolved = profile.resolve(defaults());
        assertEquals(Optional.of("pack:fodder"), resolved.pools().fodder());
        assertEquals(Optional.of("core:default_boss"), resolved.pools().boss());
        assertEquals(new CurveCoefficients(1.0, 2.0, 3.0),
                resolved.curves().difficulty().orElseThrow());
        assertEquals(new CurveCoefficients(0.0, 0.1, 0.04),
                resolved.curves().magicule().orElseThrow());
    }
}
