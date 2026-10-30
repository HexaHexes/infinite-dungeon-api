package org.bitwisemadness.infinitedungeonapi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.bitwisemadness.infinitedungeonapi.profile.DimensionProfileReloadListener;
import org.bitwisemadness.infinitedungeonapi.stats.AttributeScaling;
import org.slf4j.Logger;

/**
 * Infinite Dungeon API entry point.
 *
 * <p>This library holds the abstractions and service registries every
 * infinite-dungeon module shares: dimension profiles, the ambient magicule
 * service, entity power stat providers, and lifecycle events. It ships no
 * gameplay of its own, so it is safe to install alone.</p>
 */
@Mod(InfiniteDungeonApi.MOD_ID)
public final class InfiniteDungeonApi {

    public static final String MOD_ID = "infinite_dungeon_api";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfiniteDungeonApi(IEventBus modBus) {
        // Dimension profiles are datapack data owned by this library, so the
        // reload listener registers here rather than in a consumer.
        NeoForge.EVENT_BUS.addListener(DimensionProfileReloadListener::onAddReloadListeners);
        // The attribute scaling list enumerates Tensura's attributes on the
        // first level load - the earliest point every mod is guaranteed
        // constructed (issue api#67); registration is idempotent.
        NeoForge.EVENT_BUS.addListener(AttributeScaling::onLevelLoad);
        LOGGER.info("Infinite Dungeon API initialized");
    }
}
