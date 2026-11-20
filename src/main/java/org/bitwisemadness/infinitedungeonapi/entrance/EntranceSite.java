package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Where an entrance may generate: the position and the FIXED, predefined
 * structure that stands there (api#58). The register's rule - each dungeon
 * entrance is a fixed structure, no procedural variation - is the API's
 * contract: a provider never reports a "variation", only a structure id.
 *
 * @param position   the world position of the entrance
 * @param structure  the fixed structure's resource id
 */
public record EntranceSite(BlockPos position, ResourceLocation structure) {

    public EntranceSite {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(structure, "structure");
    }
}
