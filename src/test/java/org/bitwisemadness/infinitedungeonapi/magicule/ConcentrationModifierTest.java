package org.bitwisemadness.infinitedungeonapi.magicule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConcentrationModifierTest {

    @Test
    void additiveModifiersSum() {
        assertEquals(15.0, ConcentrationModifier.applyAll(10.0,
                List.of(ConcentrationModifier.additive(3.0), ConcentrationModifier.additive(2.0))));
    }

    @Test
    void multiplicativeModifiersMultiply() {
        assertEquals(20.0, ConcentrationModifier.applyAll(10.0,
                List.of(ConcentrationModifier.multiplicative(2.0), ConcentrationModifier.multiplicative(1.0))));
    }

    @Test
    void additivesApplyBeforeMultipliers() {
        // (10 + 5) * 2, not 10 * 2 + 5.
        assertEquals(30.0, ConcentrationModifier.applyAll(10.0,
                List.of(ConcentrationModifier.multiplicative(2.0), ConcentrationModifier.additive(5.0))));
    }

    @Test
    void stackingIsIndependentOfRegistrationOrder() {
        List<ConcentrationModifier> a = List.of(
                ConcentrationModifier.additive(5.0),
                ConcentrationModifier.multiplicative(3.0),
                ConcentrationModifier.additive(-2.0),
                ConcentrationModifier.multiplicative(0.5));
        List<ConcentrationModifier> b = List.of(
                ConcentrationModifier.multiplicative(0.5),
                ConcentrationModifier.additive(-2.0),
                ConcentrationModifier.multiplicative(3.0),
                ConcentrationModifier.additive(5.0));

        assertEquals(ConcentrationModifier.applyAll(4.0, a), ConcentrationModifier.applyAll(4.0, b));
        assertEquals(10.5, ConcentrationModifier.applyAll(4.0, a));
    }

    @Test
    void noModifiersLeavesTheBaseUntouched() {
        assertEquals(7.5, ConcentrationModifier.applyAll(7.5, List.of()));
        assertEquals(0.0, ConcentrationModifier.applyAll(0.0, List.of()));
    }

    @Test
    void applyMatchesTheBatchPhaseForASingleModifier() {
        assertEquals(ConcentrationModifier.additive(2.0).apply(3.0), 5.0);
        assertEquals(ConcentrationModifier.multiplicative(2.0).apply(3.0), 6.0);
    }

    @Test
    void scopedModifiersApplyOnlyInsideTheirRadius() {
        ConcentrationModifier near = ConcentrationModifier.additive(5.0,
                RadiusScope.around(0, 0, 0, 10.0));
        assertEquals(8.0, ConcentrationModifier.applyAllAt(3.0, List.of(near), 6, 8, 0),
                "inside the sphere the elevation applies");
        assertEquals(3.0, ConcentrationModifier.applyAllAt(3.0, List.of(near), 100, 0, 0),
                "outside the sphere the elevation does not leak");
    }

    @Test
    void phasesHoldAcrossScopedSets() {
        // The contract from the class javadoc: filter first, then the SAME two
        // phases - at the position inside, (3 + 5) * 2, never 3 * 2 + 5;
        // outside, the scoped additive drops out and only the global remains.
        List<ConcentrationModifier> mixed = List.of(
                ConcentrationModifier.multiplicative(2.0),
                ConcentrationModifier.additive(5.0, RadiusScope.around(0, 0, 0, 10.0)));
        assertEquals(16.0, ConcentrationModifier.applyAllAt(3.0, mixed, 0, 0, 0));
        assertEquals(6.0, ConcentrationModifier.applyAllAt(3.0, mixed, 500, 0, 0));
        // And registration order still does not matter inside the sphere.
        List<ConcentrationModifier> flipped = List.of(
                ConcentrationModifier.additive(5.0, RadiusScope.around(0, 0, 0, 10.0)),
                ConcentrationModifier.multiplicative(2.0));
        assertEquals(ConcentrationModifier.applyAllAt(3.0, mixed, 1, 1, 1),
                ConcentrationModifier.applyAllAt(3.0, flipped, 1, 1, 1));
    }

    @Test
    void unscopedFactoriesApplyEveryPositionThroughApplyAllAt() {
        List<ConcentrationModifier> global = List.of(
                ConcentrationModifier.additive(3.0),
                ConcentrationModifier.multiplicative(2.0));
        assertEquals((3.0 + 3.0) * 2.0, ConcentrationModifier.applyAllAt(3.0, global, 999, 0, 0));
        assertEquals((3.0 + 3.0) * 2.0, ConcentrationModifier.applyAllAt(3.0, global, -999, 64, 5));
    }

    @Test
    void positionFreeFormAppliesEveryModifierByDesign() {
        // The documented difference between the two entry points: applyAll is
        // the position-free form (correct for global-only sets), so a scoped
        // modifier passed to it contributes everywhere. Callers with
        // coordinates use applyAllAt; the service's query() does.
        ConcentrationModifier scoped = ConcentrationModifier.additive(5.0,
                RadiusScope.around(0, 0, 0, 10.0));
        assertEquals(8.0, ConcentrationModifier.applyAll(3.0, List.of(scoped)));
    }

    @Test
    void nullScopeIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConcentrationModifier(1.0, false, null));
    }
}
