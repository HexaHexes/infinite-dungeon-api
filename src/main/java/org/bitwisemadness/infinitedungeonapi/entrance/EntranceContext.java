package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

/**
 * The entrance generation query (api#58): the level, the random source the
 * rarity/spacing decisions draw from, the entrance's key (which provider
 * answers), the configured fallback, and the candidate position a provider
 * answers for. Immutable; providers read it. The position is the site the
 * provider considers - a feature with a fixed origin supplies it, a locating
 * provider leaves it empty. The level is a {@link LevelAccessor} so the same
 * query serves worldgen callers (a {@code WorldGenLevel}) and runtime callers
 * (a {@code ServerLevel}); the policy reads blocks and random, nothing else.
 */
public record EntranceContext(LevelAccessor level, RandomSource random,
                              String key, boolean fallbackGenerate,
                              Optional<BlockPos> position) {

    public EntranceContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(key, "key");
        position = position == null ? Optional.empty() : position;
    }

    /** A position-less query; the provider locates the site itself. */
    public EntranceContext(LevelAccessor level, RandomSource random,
                           String key, boolean fallbackGenerate) {
        this(level, random, key, fallbackGenerate, Optional.empty());
    }

    /** The query for a candidate position; {@code key} and fallback must be supplied. */
    public static EntranceContext at(LevelAccessor level, BlockPos pos, String key,
                                     boolean fallbackGenerate) {
        return new EntranceContext(level, level.getRandom(), key, fallbackGenerate,
                Optional.of(Objects.requireNonNull(pos, "pos")));
    }
}
