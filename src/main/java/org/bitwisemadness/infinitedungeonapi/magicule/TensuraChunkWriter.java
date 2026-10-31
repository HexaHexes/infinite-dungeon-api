package org.bitwisemadness.infinitedungeonapi.magicule;

import io.github.manasmods.tensura.storage.TensuraStorages;
import io.github.manasmods.tensura.storage.chunk.ChunkStorage;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bitwisemadness.infinitedungeonapi.InfiniteDungeonApi;

/**
 * The Tensura side of the chunk-magicule bridge (issue api#68), isolated so
 * that no Tensura or manascore type is ever loaded unless the ModList gate in
 * {@link TensuraMagiculeBridge} passed - the tier-class pattern the party
 * resolver established (api#65).
 *
 * <p><b>Spike core#6's verification checks, answered against the pinned jar
 * ({@code tensura-reincarnated-643695:8793360}) bytecode:</b></p>
 *
 * <ul>
 *   <li><b>Write lands after init:</b> {@code TensuraStorages.getChunkFrom}
 *       fetches the manascore storage and calls {@code initialize()} itself
 *       when it exists, BEFORE this class's {@code setMagicule} runs - and
 *       returns null (skipped, retried next load) when storage is not
 *       attached yet. Both orders are Tensura's own code, not ours to
 *       sequence.</li>
 *   <li><b>Survives save/load:</b> {@code ChunkStorage.save} writes the
 *       {@code magicule} field into chunk NBT and {@code load} reads it back;
 *       {@code setMagicule} is a plain field put with no clamp, and
 *       {@code getMagicule} reads that same field - so
 *       {@code AreaMagiculeHelper.getMagicule} answers with our value. The
 *       write is followed by {@code markDirty()} because {@code setMagicule}
 *       does NOT mark dirty on its own: without it the chunk may never be
 *       re-saved and the value would silently die with the session. The
 *       on-disk round trip itself is pack-verified at wiring (core #71) per
 *       the spike convention - the harness cannot load Tensura.</li>
 *   <li><b>Survives recompute:</b> {@code initialize()}/{@code reinitialize()}
 *       run {@code resetBaseValues()}, which overwrites the magicule field
 *       from the datapack config - a bare write does NOT survive them. This
 *       matches the accepted design's posture: recompute-survival is
 *       re-application on the next chunk load, not a race against Tensura's
 *       own lifecycle.</li>
 *   <li><b>{@code tick()} drift (finding for the consumer):</b> while loaded,
 *       Tensura's {@code tick()} moves the magicule field toward
 *       {@code getMaxMagicule()} at the regeneration rate - so a written M
 *       drifts toward Tensura's computed cap between loads. Recorded on core
 *       #71's wiring checklist: the pack verification must observe whether
 *       the drift matters for the spawn gate's reads before defaults ship;
 *       if it does, aligning the cap (also writing {@code maxMagicule}) is a
 *       follow-up decision, deliberately outside this mechanism.</li>
 * </ul>
 */
final class TensuraChunkWriter {

    private TensuraChunkWriter() {
    }

    /**
     * Writes the value into the chunk's Tensura storage and arms persistence.
     *
     * @return true when the storage existed and accepted the value; false
     *         when it is not attached yet (the next scheduleWrite retries)
     */
    static boolean write(double magicule, LevelChunk chunk) {
        ChunkStorage storage = TensuraStorages.getChunkFrom(chunk);
        if (storage == null) {
            // manascore has not attached this chunk's storage yet - a normal
            // race during load; the deferred write is the retry path.
            InfiniteDungeonApi.LOGGER.debug(
                    "Tensura chunk storage not attached yet at {},{} - deferring to the next load",
                    chunk.getPos().x, chunk.getPos().z);
            return false;
        }
        storage.setMagicule(magicule);
        // setMagicule alone never marks the chunk dirty; without this the
        // NBT round trip spike core#6 verified would never be armed.
        storage.markDirty();
        return true;
    }
}
