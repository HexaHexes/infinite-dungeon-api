package org.bitwisemadness.infinitedungeonapi.magicule;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
