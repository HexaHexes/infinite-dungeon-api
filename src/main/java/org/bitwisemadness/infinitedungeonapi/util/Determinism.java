package org.bitwisemadness.infinitedungeonapi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

/**
 * Named deterministic value streams (issue api#84, after the D37 comparison
 * decision): one independent positional-random stream per decision, so adding
 * a decision later can never shift any existing roll.
 *
 * <p>Each call mixes three inputs into the result - the world seed through the
 * positional factory, the decision's <b>name</b> through {@code fromHashOf},
 * and the position through {@code at} - so the same (name, seed, position)
 * yields the same values on every run and every machine, while two different
 * names at the same position share nothing by construction. The naming
 * convention is {@code namespace:decision} (for example
 * {@code infinite_dungeon:guardian_schedule}): names are case- and
 * order-significant, and a namespace keeps independent features from ever
 * colliding.</p>
 *
 * <p><b>Noise caution</b> (the comparison design's rule, adopted here as
 * documentation): noise outputs are not evenly distributed - Perlin-style
 * values cluster around the midpoint - so remap any noise value to an even
 * distribution before it drives a range or threshold decision. Positional
 * random from this class is already evenly distributed and needs no remap.</p>
 *
 * <p>Nothing migrates to these streams inside this changeset (issue api#84's
 * scope): each existing decision moves on its own issue, so worlds keep
 * their rolls while the suite adopts them one by one.</p>
 */
public final class Determinism {

    private Determinism() {
    }

    /**
     * The stream for one decision at one block position.
     *
     * @param decision  the decision's name, {@code namespace:decision} by
     *                  convention; the name is mixed into the hash, so two
     *                  decisions never see each other's values
     * @param worldSeed the dimension's world seed
     * @param at        the position the decision is about
     * @return a live random source for exactly that decision, seed and position
     */
    public static RandomSource stream(String decision, long worldSeed, BlockPos at) {
        if (at == null) {
            throw new IllegalArgumentException(
                    "a deterministic stream needs a position; got null");
        }
        return stream(decision, worldSeed, at.getX(), at.getY(), at.getZ());
    }

    /**
     * A single draw for one decision - the unit-testable surface (the test
     * source set carries no Minecraft classpath, so it cannot name
     * {@link RandomSource}), and a convenient form for decisions that need
     * exactly one value.
     *
     * @see #stream(String, long, int, int, int)
     */
    public static long nextLong(String decision, long worldSeed, int x, int y, int z) {
        return stream(decision, worldSeed, x, y, z).nextLong();
    }

    /**
     * The stream for one decision at one coordinate triple - for decisions
     * whose coordinates are not blocks (a layer and floor, a lane and cell).
     *
     * @see #stream(String, long, BlockPos)
     */
    public static RandomSource stream(String decision, long worldSeed, int x, int y, int z) {
        if (decision == null || decision.isEmpty()) {
            throw new IllegalArgumentException(
                    "a deterministic stream needs a non-empty decision name; got " + decision);
        }
        // The named source forks again before positioning, so the position
        // mixes WITH the name and the seed rather than replacing them - each
        // hop draws its entropy from the previous state (verified in bytecode).
        return RandomSource.create(worldSeed)
                .forkPositional()
                .fromHashOf(decision)
                .forkPositional()
                .at(x, y, z);
    }
}
