package org.bitwisemadness.infinitedungeonapi.profile;

/**
 * A dimension profile's layer-guardian rules.
 *
 * @param respawnCooldown   seconds a defeated guardian chamber waits before the
 *                          guardian returns, for farming
 * @param powerBandPercent  half-width of the per-layer power band, in percent,
 *                          that guardian selection must fit after depth scaling
 */
public record GuardianRules(int respawnCooldown, double powerBandPercent) {

    public GuardianRules {
        if (respawnCooldown < 0) {
            throw new IllegalArgumentException("guardian.respawnCooldown must not be negative");
        }
        if (powerBandPercent < 0.0) {
            throw new IllegalArgumentException("guardian.powerBandPercent must not be negative");
        }
    }
}
