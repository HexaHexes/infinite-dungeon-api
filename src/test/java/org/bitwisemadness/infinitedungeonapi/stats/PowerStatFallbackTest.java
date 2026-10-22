package org.bitwisemadness.infinitedungeonapi.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PowerStatFallbackTest {

    @Test
    void zeroInputsYieldZero() {
        for (StatKind kind : StatKind.values()) {
            assertEquals(0.0, PowerStatFallback.of(kind, 0.0, 0.0));
        }
    }

    @Test
    void negativeInputsAreClamped() {
        for (StatKind kind : StatKind.values()) {
            assertEquals(0.0, PowerStatFallback.of(kind, -10.0, -5.0));
        }
    }

    @Test
    void everyKindGrowsWithHealth() {
        for (StatKind kind : StatKind.values()) {
            double weak = PowerStatFallback.of(kind, 20.0, 0.0);
            double tough = PowerStatFallback.of(kind, 200.0, 0.0);
            assertTrue(tough > weak, kind + " must grow with health");
        }
    }

    @Test
    void attackScalingKindsGrowWithDamage() {
        for (StatKind kind : new StatKind[] {StatKind.AP, StatKind.EP}) {
            double weak = PowerStatFallback.of(kind, 20.0, 2.0);
            double strong = PowerStatFallback.of(kind, 20.0, 20.0);
            assertTrue(strong > weak, kind + " must grow with attack damage");
        }
    }

    @Test
    void apWeightsDamageAboveHealth() {
        assertEquals(PowerStatFallback.of(StatKind.AP, 20.0, 5.0),
                PowerStatFallback.of(StatKind.AP, 21.0, 5.0) - 0.25, 1.0e-9);
    }

    @Test
    void epCombinesHealthAndDamage() {
        assertEquals(20.0 + 3.0 * 4.0, PowerStatFallback.of(StatKind.EP, 20.0, 3.0), 1.0e-9);
    }

    @Test
    void ordersTypicalMobsSensibly() {
        double cow = PowerStatFallback.of(StatKind.EP, 10.0, 0.0);
        double zombie = PowerStatFallback.of(StatKind.EP, 20.0, 3.0);
        double dragon = PowerStatFallback.of(StatKind.EP, 200.0, 10.0);

        assertTrue(cow < zombie);
        assertTrue(zombie < dragon);
    }
}
