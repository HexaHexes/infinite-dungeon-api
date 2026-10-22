package org.bitwisemadness.infinitedungeonapi.spawn;

import java.util.Optional;

/**
 * The derived classification chain, in precedence order:
 *
 * <ol>
 *   <li>a datapack override, when one names the entity;</li>
 *   <li>the {@code NeutralMob} interface;</li>
 *   <li>the {@code Monster} class or the vanilla {@code MONSTER} category;</li>
 *   <li>passive otherwise.</li>
 * </ol>
 *
 * <p>Pure logic on purpose: actions taken against mobs depend on this, so it is
 * unit-tested with injected facts rather than exercised only in game.</p>
 */
public final class ClassificationRules {

    private ClassificationRules() {
    }

    /** Resolves the category for a set of facts and an optional override. */
    public static SpawnCategory resolve(Optional<SpawnCategory> override, ClassificationFacts facts) {
        if (override != null && override.isPresent()) {
            return override.get();
        }
        if (facts == null) {
            return SpawnCategory.PASSIVE;
        }
        if (facts.neutralMob()) {
            return SpawnCategory.NEUTRAL;
        }
        if (facts.monsterClass() || facts.monsterCategory()) {
            return SpawnCategory.HOSTILE;
        }
        return SpawnCategory.PASSIVE;
    }
}
