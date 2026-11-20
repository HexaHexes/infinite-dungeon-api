package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Optional;

/**
 * The entrance generation policy (api#58): the API owns the decision, the
 * dungeon supplies the structure. For an entrance key, the policy asks the
 * registered location provider, gates the answer through
 * {@link EntranceSuitability}, and applies the caller's EXPLICIT fallback
 * when no provider answers or the site is unsuitable - the register's
 * "never silently in unsuitable ones" and "explicit, never implicit" rules.
 *
 * <p>Pure over its inputs: the provider registry and the level. The footprint
 * (width/height/depth) and the entrance block are the caller's structure
 * facts, kept out of the policy so it stays generic.</p>
 */
public final class EntranceGenerationPolicy {

    private final int width;
    private final int height;
    private final int depth;
    private final net.minecraft.world.level.block.Block entranceBlock;

    public EntranceGenerationPolicy(int width, int height, int depth,
                                    net.minecraft.world.level.block.Block entranceBlock) {
        if (width < 1 || height < 1 || depth < 1) {
            throw new IllegalArgumentException(
                    "the footprint must be positive, got [" + width + ", " + height + ", " + depth + "]");
        }
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.entranceBlock = java.util.Objects.requireNonNull(entranceBlock, "entranceBlock");
    }

    /** The decision for the entrance key: provider answer -> suitability -> explicit fallback. */
    public EntranceDecision decide(EntranceContext context) {
        Optional<EntranceLocationProvider> provider = EntranceLocations.get(context.key());
        if (provider.isEmpty()) {
            // The explicit, never implicit fallback: the caller's configured
            // behaviour, never a guess.
            return context.fallbackGenerate()
                    ? EntranceDecision.fallbackGenerate() : EntranceDecision.skip();
        }
        Optional<EntranceSite> site = provider.get().locate(context);
        if (site.isEmpty()) {
            return context.fallbackGenerate()
                    ? EntranceDecision.fallbackGenerate() : EntranceDecision.skip();
        }
        if (!EntranceSuitability.isSuitable(context.level(), site.get(),
                width, height, depth, entranceBlock)) {
            return EntranceDecision.skip(); // never silently generate in an unsuitable context
        }
        return EntranceDecision.generate(site.get());
    }
}
