package org.bitwisemadness.infinitedungeonapi.profile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.bitwisemadness.infinitedungeonapi.data.InfiniteDungeonJsonReloadListener;
import org.slf4j.Logger;

/**
 * Loads {@code data/<namespace>/infinite_dungeon/dimension_profile/*.json} on
 * server start and on every {@code /reload}.
 *
 * <p>The base listener parses in sorted location order and skips malformed
 * files, never fatal. Here the entries are additionally validated - a profile
 * whose dimension id is not a resource location is skipped, and pool ids that
 * are not well-formed are dropped - so a profile can name optional content
 * without breaking the load. The table is rebuilt from scratch each load, so a
 * reload that drops a file also drops what it declared.</p>
 */
public final class DimensionProfileReloadListener extends InfiniteDungeonJsonReloadListener<DimensionProfile> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DimensionProfileReloadListener() {
        super(new Gson(), "dimension_profile");
    }

    /** Registers the listener; called from the mod constructor. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DimensionProfileReloadListener());
    }

    @Override
    protected DimensionProfile parse(ResourceLocation location, JsonElement json) {
        return DimensionProfileCodecs.PROFILE.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    @Override
    protected void applyEntries(Map<ResourceLocation, DimensionProfile> entries) {
        Map<String, DimensionProfile> byDimension = new TreeMap<>();
        entries.forEach((location, parsed) -> {
            if (!parsed.hasWellFormedDimension()) {
                LOGGER.warn("Skipping dimension profile {}: '{}' is not a valid dimension id",
                        location, parsed.dimension());
                return;
            }
            DimensionProfile profile = parsed;
            List<String> malformed = profile.malformedPoolIds();
            if (!malformed.isEmpty()) {
                LOGGER.warn("Dimension profile {} drops unknown pool reference(s): {}",
                        location, malformed);
                profile = profile.sanitised();
            }
            DimensionProfile previous = byDimension.put(profile.dimension(), profile);
            if (previous != null) {
                LOGGER.warn("Duplicate dimension profile for {}: {} wins",
                        profile.dimension(), location);
            }
        });
        DimensionProfiles.setDatapack(byDimension);
        LOGGER.debug("Loaded {} dimension profile(s) from datapacks", byDimension.size());
    }
}
