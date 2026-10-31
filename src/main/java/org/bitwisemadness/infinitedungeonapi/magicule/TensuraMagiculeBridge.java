package org.bitwisemadness.infinitedungeonapi.magicule;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;

/**
 * The Tensura chunk-magicule write bridge (issue api#68): feeds the M
 * concentration into Tensura's per-chunk value so in-dungeon ambient reads
 * answer with M. This class is the facade - no Tensura type appears in any
 * signature or field, so loading it (and calling it on a pack without
 * Tensura) never touches a foreign class; the ModList gate routes every write
 * to {@link TensuraChunkWriter}, the only class that names Tensura types.
 *
 * <p><b>Division of labour (api#68 vs core #71):</b> this bridge builds the
 * mechanism - evaluate M, defer past Tensura's storage init, write. The gate
 * (WHETHER to write, inside which dimension) lives with the consumer, core
 * #71: the plan's phrasing is "inside our dimension", and I32 keeps that
 * decision with the mod that owns the dimension. The bridge writes wherever
 * it is called.</p>
 *
 * <p><b>Timing (spike core#6):</b> Tensura's {@code initialize()},
 * {@code reinitialize()} and {@code tick()} recompute base values from
 * datapack modifiers, so a write must land after storage init. The bridge
 * defers via {@link #defer}, next-tick scheduling on the server queue: a
 * chunk-load listener (core #71's) calls {@link #scheduleWrite}, the write
 * runs once the current tick's loading has finished, and
 * {@code TensuraStorages.getChunkFrom} initializes storage before the value
 * lands - both orders proven against the pinned jar and recorded on
 * {@link TensuraChunkWriter}.</p>
 */
public final class TensuraMagiculeBridge {

    /** Tensura's mod id, verified from the pinned jar's mods.toml. */
    private static final String TENSURA_MOD_ID = "tensura";

    private TensuraMagiculeBridge() {
    }

    /** Whether Tensura is present and the write path may run at all. */
    public static boolean isUsable() {
        return ModList.get().isLoaded(TENSURA_MOD_ID);
    }

    /**
     * The M value this bridge writes for a chunk: {@link AmbientMagicule}
     * sampled at the chunk centre's WORLD_SURFACE height - the same
     * representative position the dungeon-mouth elevation sources use
     * (#218): where mobs actually spawn is where the value should answer.
     * Exposed so consumers and tests can see exactly what a write would
     * carry before anything is written.
     */
    public static double magiculeAt(ServerLevel level, LevelChunk chunk) {
        int centerX = chunk.getPos().getMinBlockX() + 8;
        int centerZ = chunk.getPos().getMinBlockZ() + 8;
        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8);
        return AmbientMagicule.query(level, new BlockPos(centerX, surfaceY, centerZ));
    }

    /**
     * Writes M into the chunk's Tensura storage immediately. Idempotent:
     * re-writes converge on the same value, so repeated loads re-apply rather
     * than stack.
     *
     * @return true when the value was written; false when Tensura is absent
     *         or its storage is not attached yet (the next load retries)
     */
    public static boolean writeChunk(ServerLevel level, LevelChunk chunk) {
        if (!isUsable()) {
            return false;
        }
        return TensuraChunkWriter.write(magiculeAt(level, chunk), chunk);
    }

    /**
     * The chunk-load entry point: schedules {@link #writeChunk} for the next
     * tick so the write lands after Tensura's storage initialization, which
     * may itself be running during this load. Safe to call from any thread
     * and from multiple loads of the same chunk - the write is idempotent.
     */
    public static void scheduleWrite(ServerLevel level, LevelChunk chunk) {
        // ServerLevel.getServer() is non-null by type (SpotBugs itself proved
        // the redundant branch redundant) - no fallback needed.
        defer(level.getServer(), () -> writeChunk(level, chunk));
    }

    /**
     * The defer primitive: runs the task on the server's queue at the next
     * tick boundary - never inline at the call site ({@code tell} enqueues
     * unconditionally; {@code execute} would run inline when
     * {@code scheduleExecutables()} says no, which is exactly what must not
     * happen inside a chunk-load event). This is what moves a write out of a
     * load event (possibly on a worker thread, possibly before Tensura has
     * attached its storage) and past initialization; exposed so consumers can
     * schedule their own post-load work with the same guarantee the bridge
     * uses.
     */
    public static void defer(MinecraftServer server, Runnable task) {
        server.tell(new TickTask(server.getTickCount(), task));
    }
}
