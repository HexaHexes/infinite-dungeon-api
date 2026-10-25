package org.bitwisemadness.infinitedungeonapi.profile;

/**
 * One curve's coefficients, in the core plan's shared shape
 * {@code value(L, f) = base + linearPerFloor * f + quadraticPerLayer * L^2}.
 *
 * <p>The three curves - difficulty (D), magicule (M), and density (S) - share
 * this shape with independent coefficients, so one value type describes an
 * override for any of them.</p>
 *
 * @param base               the offset at floor zero of layer zero
 * @param linearPerFloor     the within-layer term
 * @param quadraticPerLayer  the between-layer term
 */
public record CurveCoefficients(double base,
                                double linearPerFloor,
                                double quadraticPerLayer) {
}
