package org.bitwisemadness.infinitedungeonapi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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
        LOGGER.info("Infinite Dungeon API initialized");
    }
}
