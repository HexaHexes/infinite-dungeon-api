package org.bitwisemadness.infinitedungeonapi.magicule;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The suite's ambient magicule service: one question - how much magicule is at
 * this position - answered by whoever can answer it.
 *
 * <ul>
 *   <li>Providers are asked in registration order; the first non-empty answer is
 *       the base value.</li>
 *   <li>Concentration modifiers (structure and portal elevation) then adjust the
 *       base: additive modifiers are summed first, multiplicative ones multiply
 *       afterwards, so stacking does not depend on registration order.</li>
 *   <li>With no provider answer the result is {@link #COLD}, the documented cold
 *       value that makes every consumer a no-op.</li>
 * </ul>
 */
public final class AmbientMagicule {

    /** The concentration reported when nothing provides or modifies a value. */
    public static final double COLD = 0.0;

    private static final List<MagiculeProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<ConcentrationModifier> MODIFIERS = new CopyOnWriteArrayList<>();

    private AmbientMagicule() {
    }

    /** Registers a provider; providers are queried in registration order. */
    public static void registerProvider(MagiculeProvider provider) {
        PROVIDERS.add(provider);
    }

    /** Removes a provider, for example when its module is unloaded. */
    public static void removeProvider(MagiculeProvider provider) {
        PROVIDERS.remove(provider);
    }

    /** Registers a concentration modifier such as a structure's elevation. */
    public static void registerModifier(ConcentrationModifier modifier) {
        MODIFIERS.add(modifier);
    }

    /** Removes a previously registered modifier. */
    public static void removeModifier(ConcentrationModifier modifier) {
        MODIFIERS.remove(modifier);
    }

    /** The unmodified base value at a position, before concentration modifiers. */
    public static OptionalDouble baseAt(Level level, BlockPos pos) {
        for (MagiculeProvider provider : PROVIDERS) {
            OptionalDouble answer = provider.at(level, pos);
            if (answer != null && answer.isPresent()) {
                return answer;
            }
        }
        return OptionalDouble.empty();
    }

    /** The final concentration at a position: provider answer plus modifiers. */
    public static double query(Level level, BlockPos pos) {
        OptionalDouble base = baseAt(level, pos);
        return applyModifiers(base.isPresent() ? base.getAsDouble() : COLD, MODIFIERS);
    }

    /**
     * The pure modifier phase, exposed for consumers that already hold a base
     * value. Delegates to {@link ConcentrationModifier#applyAll}.
     */
    public static double applyModifiers(double base, Collection<ConcentrationModifier> modifiers) {
        return ConcentrationModifier.applyAll(base, modifiers);
    }

    /** An unmodifiable view of the registered providers. */
    public static List<MagiculeProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    /** An unmodifiable view of the registered modifiers. */
    public static List<ConcentrationModifier> modifiers() {
        return List.copyOf(MODIFIERS);
    }

    /** Clears every registration; used by reload and tests. */
    public static void reset() {
        PROVIDERS.clear();
        MODIFIERS.clear();
    }
}
