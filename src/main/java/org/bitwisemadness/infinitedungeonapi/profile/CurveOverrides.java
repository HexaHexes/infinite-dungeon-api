package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Optional;

/**
 * A dimension profile's per-curve coefficient overrides.
 *
 * <p>Each curve is independent, so a profile may override difficulty alone and
 * inherit the magicule and density defaults; an absent curve falls back to the
 * default.</p>
 *
 * @param difficulty the D override
 * @param magicule   the M override
 * @param density    the S override
 */
public record CurveOverrides(Optional<CurveCoefficients> difficulty,
                             Optional<CurveCoefficients> magicule,
                             Optional<CurveCoefficients> density) {

    public CurveOverrides {
        difficulty = nonNull(difficulty);
        magicule = nonNull(magicule);
        density = nonNull(density);
    }

    /** Overrides that replace no curve. */
    public static CurveOverrides empty() {
        return new CurveOverrides(Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static Optional<CurveCoefficients> nonNull(Optional<CurveCoefficients> value) {
        return value == null ? Optional.empty() : value;
    }
}
