package org.bitwisemadness.infinitedungeonapi.stats;

import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The suite's entity power stat service: who is stronger, in one number.
 *
 * <ul>
 *   <li>Providers are asked in registration order; the first non-empty answer
 *       wins.</li>
 *   <li>When no provider answers, {@link #fallback} derives an approximate value
 *       from vanilla attributes so ordering still works without a stats
 *       provider.</li>
 *   <li>{@link #providerValue} skips the fallback for consumers that must know
 *       whether a real provider spoke (the spawn gate treats "no provider" as
 *       zero, keeping itself a no-op).</li>
 * </ul>
 */
public final class EntityPowerStats {

    private static final List<EntityPowerStatProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private EntityPowerStats() {
    }

    /** Registers a provider; providers are queried in registration order. */
    public static void registerProvider(EntityPowerStatProvider provider) {
        PROVIDERS.add(provider);
    }

    /** Removes a previously registered provider. */
    public static void removeProvider(EntityPowerStatProvider provider) {
        PROVIDERS.remove(provider);
    }

    /** The provider-supplied value, before any fallback. */
    public static OptionalDouble providerValue(LivingEntity entity, StatKind kind) {
        for (EntityPowerStatProvider provider : PROVIDERS) {
            OptionalDouble answer = provider.of(entity, kind);
            if (answer != null && answer.isPresent()) {
                return answer;
            }
        }
        return OptionalDouble.empty();
    }

    /** The provider value, or the documented fallback when no provider answers. */
    public static double of(LivingEntity entity, StatKind kind) {
        OptionalDouble value = providerValue(entity, kind);
        return value.isPresent() ? value.getAsDouble() : fallback(entity, kind);
    }

    /** The approximate attribute-derived value; see {@link PowerStatFallback}. */
    public static double fallback(LivingEntity entity, StatKind kind) {
        double health = attributeValue(entity, Attributes.MAX_HEALTH);
        double attack = attributeValue(entity, Attributes.ATTACK_DAMAGE);
        return PowerStatFallback.of(kind, health, attack);
    }

    /** An unmodifiable view of the registered providers. */
    public static List<EntityPowerStatProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    /** Clears every registration; used by reload and tests. */
    public static void reset() {
        PROVIDERS.clear();
    }

    private static double attributeValue(LivingEntity entity,
                                         net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        var instance = entity.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }
}
