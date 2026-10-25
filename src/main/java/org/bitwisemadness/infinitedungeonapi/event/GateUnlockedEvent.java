package org.bitwisemadness.infinitedungeonapi.event;

import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A gate transitioned to open.
 *
 * <p>Notification only.</p>
 */
public final class GateUnlockedEvent extends DungeonEvent {

    private final String gateId;

    public GateUnlockedEvent(ResourceKey<Level> dimension, String gateId) {
        super(dimension);
        String id = gateId == null ? "" : gateId.trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("gateId is required");
        }
        this.gateId = id;
    }

    /** The namespaced gate id. */
    public String gateId() {
        return gateId;
    }
}
