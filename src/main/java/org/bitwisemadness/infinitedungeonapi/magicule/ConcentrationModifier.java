package org.bitwisemadness.infinitedungeonapi.magicule;

import java.util.Collection;

/**
 * One contribution to the ambient magicule concentration of an area.
 *
 * <p>Additive modifiers are summed and applied to the base value first, then
 * multiplicative modifiers multiply the result. Because the two groups are
 * applied in phases rather than registration order, the outcome does not depend
 * on the order modules register their modifiers, which keeps stacking
 * predictable for pack authors.</p>
 *
 * @param amount         the additive amount or the multiplicative factor
 * @param multiplicative whether this modifier multiplies rather than adds
 */
public record ConcentrationModifier(double amount, boolean multiplicative) {

    /** A modifier that adds {@code amount} to the concentration. */
    public static ConcentrationModifier additive(double amount) {
        return new ConcentrationModifier(amount, false);
    }

    /** A modifier that multiplies the concentration by {@code factor}. */
    public static ConcentrationModifier multiplicative(double factor) {
        return new ConcentrationModifier(factor, true);
    }

    /** Applies this modifier to a running value. */
    public double apply(double current) {
        return multiplicative ? current * amount : current + amount;
    }

    /**
     * The pure modifier phase: additive modifiers are summed, then
     * multiplicative modifiers are applied, so the result does not depend on
     * registration order. Lives here, on the dependency-free value type, so it
     * stays unit-testable without a Minecraft runtime.
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
}
