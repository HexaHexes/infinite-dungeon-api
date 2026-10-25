package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Locale;

/**
 * A dimension profile's floor-count bounds for one layer.
 *
 * <p>The actual count stays seed-derived per layer (the core plan's
 * determinism rule); these bounds are the range the derivation draws from, and
 * the core config supplies the default pair.</p>
 *
 * @param min the fewest floors a layer may have
 * @param max the most floors a layer may have
 */
public record FloorsPerLayer(int min, int max) {

    public FloorsPerLayer {
        if (min < 1) {
            throw new IllegalArgumentException("floorsPerLayer.min must be at least 1");
        }
        if (max < min) {
            throw new IllegalArgumentException("floorsPerLayer.max must not be below min");
        }
    }

    /** A human-readable range, for diagnostics. */
    public String describe() {
        return String.format(Locale.ROOT, "%d..%d", min, max);
    }
}
