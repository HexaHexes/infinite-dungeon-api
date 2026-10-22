package org.bitwisemadness.infinitedungeonapi.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/**
 * Base listener for the suite's datapack types, which live under
 * {@code data/<namespace>/infinite_dungeon/<type>/*.json} and reload with
 * {@code /reload}.
 *
 * <p>All packs' files are collected by the vanilla loader; entries are then
 * parsed in sorted location order and applied as one unmodifiable map. A file
 * that fails to parse is logged and skipped, never fatal, so a pack can carry
 * data for optional content.</p>
 *
 * @param <T> the parsed entry type
 */
public abstract class InfiniteDungeonJsonReloadListener<T> extends SimpleJsonResourceReloadListener {

    /** Root directory every suite data type shares. */
    public static final String DATA_ROOT = "infinite_dungeon";

    private static final Logger LOGGER = LogUtils.getLogger();

    protected InfiniteDungeonJsonReloadListener(Gson gson, String typeDirectory) {
        super(gson, DATA_ROOT + "/" + typeDirectory);
    }

    @Override
    protected final void apply(Map<ResourceLocation, JsonElement> files,
                               ResourceManager resourceManager,
                               ProfilerFiller profiler) {
        Map<ResourceLocation, T> parsed = new TreeMap<>(ResourceLocation::compareTo);
        files.forEach((location, json) -> {
            try {
                T entry = parse(location, json);
                if (entry != null) {
                    parsed.put(location, entry);
                }
            } catch (Exception e) {
                LOGGER.error("Skipping malformed {} data entry {}", DATA_ROOT, location, e);
            }
        });
        applyEntries(Collections.unmodifiableMap(parsed));
    }

    /** Parses one file; returning null skips it. */
    protected abstract T parse(ResourceLocation location, JsonElement json);

    /** Applies the parsed entries in sorted location order. */
    protected abstract void applyEntries(Map<ResourceLocation, T> entries);
}
