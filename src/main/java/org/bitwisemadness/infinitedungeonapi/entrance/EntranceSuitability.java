package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Objects;

/**
 * The terrain-suitability contract (api#58): an entrance may generate only in
 * suitable, selected contexts, never silently in unsuitable ones. Suitability
 * is the API's policy, computed from the level around the site - the
 * register's "minimal terrain alteration" rule: the ground under the footprint
 * must be solid, the air column above must be clear (no conflicts), and the
 * site must not already host an entrance.
 */
public final class EntranceSuitability {

    private EntranceSuitability() {
    }

    /**
     * Whether the site's footprint is suitable: solid ground under the full
     * footprint, a clear column up to the building's height, and no existing
     * entrance block at the position. Pure over the level; worldgen-safe.
     */
    public static boolean isSuitable(net.minecraft.server.level.ServerLevel level,
                                     EntranceSite site, int width, int height, int depth,
                                     net.minecraft.world.level.block.Block entranceBlock) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(site, "site");
        Objects.requireNonNull(entranceBlock, "entranceBlock");
        var base = site.position();
        if (level.getBlockState(base).is(entranceBlock)) {
            return false; // never stack an entrance on an existing entrance
        }
        int halfW = width / 2;
        int halfD = depth / 2;
        for (int dx = -halfW; dx <= halfW; dx++) {
            for (int dz = -halfD; dz <= halfD; dz++) {
                var ground = base.offset(dx, -1, dz);
                if (!level.getBlockState(ground).isSolid()) {
                    return false; // the ground must be solid under the whole footprint
                }
                for (int dy = 0; dy < height; dy++) {
                    var cell = base.offset(dx, dy, dz);
                    if (!level.getBlockState(cell).isAir()) {
                        return false; // the column must be clear - minimal alteration
                    }
                }
            }
        }
        return true;
    }
}
