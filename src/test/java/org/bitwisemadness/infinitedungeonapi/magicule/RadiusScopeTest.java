package org.bitwisemadness.infinitedungeonapi.magicule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The spherical scope (plan section 7: elevation sources carry amount and
 * radius): containment, boundary, validation, and the degenerate GLOBAL form
 * that preserves the positionless contract - all pure, no Minecraft runtime.
 */
class RadiusScopeTest {

    @Test
    void containsPositionsInsideAndOnTheBoundary() {
        RadiusScope around = RadiusScope.around(0, 0, 0, 10.0);
        assertTrue(around.contains(0, 0, 0), "the centre is inside");
        assertTrue(around.contains(6, 8, 0), "distance exactly 10 is the boundary - inside");
        assertFalse(around.contains(6, 8, 1), "beyond the radius is outside");
        assertFalse(around.contains(11, 0, 0));
    }

    @Test
    void centresAnywhereInclusiveNegative() {
        RadiusScope scope = RadiusScope.around(100, -64, -200, 5.0);
        assertTrue(scope.contains(100, -64, -200));
        assertTrue(scope.contains(103, -64, -200));
        assertFalse(scope.contains(106, -64, -200));
        // Centre on negative coordinates works the same: (-8, 64, -8) within
        // 5 of (-5, 64, -5) is distance sqrt(18) - inside.
        RadiusScope negative = RadiusScope.around(-5, 64, -5, 5.0);
        assertTrue(negative.contains(-8, 64, -8));
        assertFalse(negative.contains(-11, 64, -5));
    }

    @Test
    void zeroRadiusIsASingleBlock() {
        RadiusScope point = RadiusScope.around(7, 8, 9, 0.0);
        assertTrue(point.contains(7, 8, 9));
        assertFalse(point.contains(7, 8, 10));
    }

    @Test
    void globalAppliesEverywhere() {
        assertTrue(RadiusScope.GLOBAL.contains(0, 0, 0));
        assertTrue(RadiusScope.GLOBAL.contains(3_000_000, 2000, -3_000_000),
                "the degenerate positionless form reaches every position");
        assertTrue(RadiusScope.GLOBAL.contains(-2_000_000, -64, 2_000_000));
    }

    @Test
    void negativeRadiusIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> RadiusScope.around(0, 0, 0, -1.0));
    }

    @Test
    void recordsCompareByComponents() {
        assertEquals(RadiusScope.around(1, 2, 3, 4.0), RadiusScope.around(1, 2, 3, 4.0));
        assertFalse(RadiusScope.around(1, 2, 3, 4.0).equals(RadiusScope.around(1, 2, 3, 5.0)));
    }
}
