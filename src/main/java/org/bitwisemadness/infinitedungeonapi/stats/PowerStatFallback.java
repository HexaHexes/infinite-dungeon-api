package org.bitwisemadness.infinitedungeonapi.stats;

/**
 * The documented fallback metric used when no stats provider answers.
 *
 * <p>It is an approximation meant for ordering and band fitting, not for
 * balance: it derives a single number from vanilla health and attack damage so
 * a pack without a stats provider still gets a usable boss roster ordering.</p>
 */
public final class PowerStatFallback {

    private PowerStatFallback() {
    }

    /**
     * Derives a power value from health and attack damage.
     *
     * <ul>
     *   <li>{@code AP} weights attack damage, with a small health contribution.</li>
     *   <li>{@code MP} derives from health alone, standing in for a caster's pool.</li>
     *   <li>{@code EP} combines both, matching its role as overall power.</li>
     * </ul>
     */
    public static double of(StatKind kind, double maxHealth, double attackDamage) {
        double health = Math.max(0.0, maxHealth);
        double attack = Math.max(0.0, attackDamage);
        return switch (kind) {
            case AP -> attack * 4.0 + health * 0.25;
            case MP -> health * 0.5;
            case EP -> health + attack * 4.0;
        };
    }
}
