package org.bitwisemadness.infinitedungeonapi.magicule;

import java.util.Collection;

/**
 * One contribution to the ambient magicule concentration of an area,
 * optionally scoped to a {@link RadiusScope} neighbourhood (plan section 7;
 * hexa issue #73).
 *
 * <p>Additive modifiers are summed and applied to the base value first, then
 * multiplicative modifiers multiply the result. Because the two groups are
 * applied in phases rather than registration order, the outcome does not depend
 * on the order modules register their modifiers, which keeps stacking
 * predictable for pack authors - and the phase contract holds identically for
 * position-scoped evaluation: {@link #applyAllAt} filters first, then runs the
 * same two phases over whatever applies at the position.</p>
 *
 * <p>The type stays dependency-free (primitives and the pure RadiusScope) so
 * the unit suites load it without a Minecraft runtime. Factories without a
 * scope produce the degenerate everywhere-modifier, keeping the positionless
 * contract: {@link #applyAll} applies every modifier regardless of scope, and
 * callers holding coordinates - the service's {@code query} - must use
 * {@link #applyAllAt} so scoped modifiers stay local.</p>
 *
 * @param amount         the additive amount or the multiplicative factor
 * @param multiplicative whether this modifier multiplies rather than adds
 * @param scope          the area this modifier applies to; {@link RadiusScope#GLOBAL}
 *                       for everywhere
 */
public record ConcentrationModifier(double amount, boolean multiplicative, RadiusScope scope) {

    public ConcentrationModifier {
        if (scope == null) {
            throw new IllegalArgumentException(
                    "scope must not be null - use RadiusScope.GLOBAL for everywhere");
        }
    }

    /** A modifier that adds {@code amount} everywhere. */
    public static ConcentrationModifier additive(double amount) {
        return additive(amount, RadiusScope.GLOBAL);
    }

    /** A modifier that adds {@code amount} only inside {@code scope}. */
    public static ConcentrationModifier additive(double amount, RadiusScope scope) {
        return new ConcentrationModifier(amount, false, scope);
    }

    /** A modifier that multiplies by {@code factor} everywhere. */
    public static ConcentrationModifier multiplicative(double factor) {
        return multiplicative(factor, RadiusScope.GLOBAL);
    }

    /** A modifier that multiplies by {@code factor} only inside {@code scope}. */
    public static ConcentrationModifier multiplicative(double factor, RadiusScope scope) {
        return new ConcentrationModifier(factor, true, scope);
    }

    /** The positionless constructor: the modifier applies everywhere. */
    public ConcentrationModifier(double amount, boolean multiplicative) {
        this(amount, multiplicative, RadiusScope.GLOBAL);
    }

    /** Whether this modifier applies at the position (scope test only). */
    public boolean appliesAt(int x, int y, int z) {
        return scope.contains(x, y, z);
    }

    /** Applies this modifier to a running value. */
    public double apply(double current) {
        return multiplicative ? current * amount : current + amount;
    }

    /**
     * The position-free form: every modifier applies, scoped or not. Correct
     * for global-only sets; callers with coordinates must prefer
     * {@link #applyAllAt} so scoped modifiers stay local.
     *
     * <p>The pure phase math: additive modifiers are summed, then
     * multiplicative modifiers are applied, so the result does not depend on
     * registration order. Lives here, on the dependency-free value type, so it
     * stays unit-testable without a Minecraft runtime.</p>
     */
    public static double applyAll(double base, Collection<ConcentrationModifier> modifiers) {
        double additive = 0.0;
        double multiplier = 1.0;
        for (ConcentrationModifier modifier : modifiers) {
            if (modifier.multiplicative()) {
                multiplier *= modifier.amount();
            } else {
                additive += modifier.amount();
            }
        }
        return (base + additive) * multiplier;
    }

    /**
     * The position-aware form: only the modifiers whose {@link RadiusScope}
     * contains the position participate, then the same two phases run - so the
     * phase contract (add before multiply, order-independent) holds within
     * every neighbourhood exactly as it does globally.
     *
     * @param base the provider's base value
     * @param modifiers the registered modifiers, scoped and global alike
     * @param x the position's world X
     * @param y the position's world Y
     * @param z the position's world Z
     * @return the modified concentration at the position
     */
    public static double applyAllAt(double base, Collection<ConcentrationModifier> modifiers,
            int x, int y, int z) {
        double additive = 0.0;
        double multiplier = 1.0;
        for (ConcentrationModifier modifier : modifiers) {
            if (!modifier.appliesAt(x, y, z)) {
                continue;
            }
            if (modifier.multiplicative()) {
                multiplier *= modifier.amount();
            } else {
                additive += modifier.amount();
            }
        }
        return (base + additive) * multiplier;
    }
}
