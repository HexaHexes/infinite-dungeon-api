package org.bitwisemadness.infinitedungeonapi.dialogue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.bitwisemadness.infinitedungeonapi.data.InfiniteDungeonJsonReloadListener;
import org.slf4j.Logger;

/**
 * Loads {@code data/<namespace>/infinite_dungeon/dialogue/*.json} on server
 * start and on every {@code /reload}.
 *
 * <p>The base listener parses in sorted location order and skips malformed
 * files, never fatal. The table is rebuilt from scratch each load, so a
 * reload that drops a file also drops what it declared.</p>
 */
public final class DialogueReloadListener extends InfiniteDungeonJsonReloadListener<DialogueEntry> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DialogueReloadListener() {
        super(new Gson(), "dialogue");
    }

    /** Registers the listener; called from the mod constructor. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DialogueReloadListener());
    }

    @Override
    protected DialogueEntry parse(ResourceLocation location, JsonElement json) {
        return DialogueCodecs.ENTRY.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    @Override
    protected void applyEntries(Map<ResourceLocation, DialogueEntry> entries) {
        Map<String, DialogueEntry> byKey = new TreeMap<>();
        entries.forEach((location, entry) -> byKey.put(location.toString(), entry));
        Dialogues.setDatapack(byKey);
        LOGGER.debug("Loaded {} dialogue entries from datapacks", byKey.size());
    }
}