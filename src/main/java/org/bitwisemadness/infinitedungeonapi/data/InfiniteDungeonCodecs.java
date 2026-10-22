package org.bitwisemadness.infinitedungeonapi.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;

/**
 * JSON codecs shared by every suite data type.
 *
 * <p>Codecs live here rather than on the value types so those types stay free of
 * Mojang and Minecraft dependencies and remain unit-testable without a game
 * runtime (see {@link ResourceRef}).</p>
 */
public final class InfiniteDungeonCodecs {

    private InfiniteDungeonCodecs() {
    }

    /** Reads and writes {@code "ns:path"} or {@code "#ns:path"} references. */
    public static final Codec<ResourceRef> RESOURCE_REF =
            Codec.STRING.xmap(ResourceRef::parse, ResourceRef::serialize);

    /** The shared optional {@code required_mod} field. */
    public static final MapCodec<Optional<String>> REQUIRED_MOD =
            Codec.STRING.optionalFieldOf("required_mod");
}
