package org.bitwisemadness.infinitedungeonapi.spawn;

/**
 * The observable facts used to derive a mob's {@link SpawnCategory}.
 *
 * <p>Kept as plain booleans so classification precedence is pure logic and
 * unit-testable with injected values; the game-facing code gathers the facts.</p>
 *
 * @param neutralMob    the entity implements the {@code NeutralMob} interface
 * @param monsterClass  the entity is an instance of {@code Monster}
 * @param monsterCategory the entity's vanilla category is {@code MONSTER}
 */
public record ClassificationFacts(boolean neutralMob, boolean monsterClass, boolean monsterCategory) {

    /** Convenience for the common "nothing matches" case. */
    public static ClassificationFacts none() {
        return new ClassificationFacts(false, false, false);
    }
}
