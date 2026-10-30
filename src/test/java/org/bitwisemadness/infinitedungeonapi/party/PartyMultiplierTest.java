package org.bitwisemadness.infinitedungeonapi.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * The party multiplier's arithmetic (plan section 8; issue api#65): solo is
 * exactly 1.0, each additional present member adds perMember, and the edges
 * are loud - pure math, no Minecraft runtime (the standing rule).
 */
public class PartyMultiplierTest {

    private static final double EPS = 1e-9;

    @Test
    void soloIsExactlyOne() {
        assertEquals(1.0, PartyMultiplier.of(1, 0.5), EPS);
        assertEquals(1.0, PartyMultiplier.of(1, 0.0), EPS);
        assertEquals(1.0, PartyMultiplier.of(1, 3.0), EPS);
    }

    @Test
    void eachAdditionalMemberAddsPerMember() {
        assertEquals(2.0, PartyMultiplier.of(3, 0.5), EPS);
        assertEquals(2.5, PartyMultiplier.of(3, 0.75), EPS);
        assertEquals(4.0, PartyMultiplier.of(9, 0.375), EPS);
        // Zero increment: party size never weakens or strengthens alone.
        assertEquals(1.0, PartyMultiplier.of(7, 0.0), EPS);
    }

    @Test
    void zeroPresentStillMeansSolo() {
        // The facade floors present at 1 (the anchor counts itself); the
        // arithmetic refuses to invent negatives on the way down.
        assertEquals(1.0, PartyMultiplier.of(0, 0.5), EPS);
    }

    @Test
    void negativePresentIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> PartyMultiplier.of(-1, 0.5));
    }
}
