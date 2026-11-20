package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

/**
 * The entrance generation query (api#58): the level, the random source the
 * rarity/spacing decisions draw from, the entrance's key (which provider
 * answers), and the configured fallback. Immutable; providers read it.
 */
public record EntranceContext(ServerLevel level, RandomSource random,
                              String key, boolean fallbackGenerate) {

    public EntranceContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(key, "key");
    }

    /** The query at a position; {@code key} and fallback must be supplied. */
    public static EntranceContext at(ServerLevel level, BlockPos pos, String key,
                                     boolean fallbackGenerate) {
        return new EntranceContext(level, level.getRandom(), key, fallbackGenerate);
    }
}
