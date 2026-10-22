package org.bitwisemadness.infinitedungeonapi.spawn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClassificationRulesTest {

    private static final Optional<SpawnCategory> NO_OVERRIDE = Optional.empty();

    @Test
    void monsterClassIsHostile() {
        assertEquals(SpawnCategory.HOSTILE, ClassificationRules.resolve(NO_OVERRIDE,
                new ClassificationFacts(false, true, false)));
    }

    @Test
    void monsterCategoryIsHostile() {
        assertEquals(SpawnCategory.HOSTILE, ClassificationRules.resolve(NO_OVERRIDE,
                new ClassificationFacts(false, false, true)));
    }

    @Test
    void neutralMobBeatsMonster() {
        assertEquals(SpawnCategory.NEUTRAL, ClassificationRules.resolve(NO_OVERRIDE,
                new ClassificationFacts(true, true, true)));
    }

    @Test
    void nothingMatchesIsPassive() {
        assertEquals(SpawnCategory.PASSIVE, ClassificationRules.resolve(NO_OVERRIDE,
                ClassificationFacts.none()));
        assertEquals(SpawnCategory.PASSIVE, ClassificationRules.resolve(NO_OVERRIDE, null));
    }

    @Test
    void overrideBeatsEveryDerivedFact() {
        for (SpawnCategory override : SpawnCategory.values()) {
            assertEquals(override, ClassificationRules.resolve(Optional.of(override),
                    new ClassificationFacts(true, true, true)));
        }
    }

    @Test
    void nullOverrideMeansNoOverride() {
        assertEquals(SpawnCategory.HOSTILE, ClassificationRules.resolve(null,
                new ClassificationFacts(false, true, false)));
    }
}
