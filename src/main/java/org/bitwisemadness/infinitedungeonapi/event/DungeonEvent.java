package org.bitwisemadness.infinitedungeonapi.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/**
 * Base for the suite's lifecycle notifications.
 *
 * <p>Every event names the dimension it happened in. These are game-facing
 * types, fired server-side by the core module and observed by integrations.</p>
 *
 * <p>Events are notifications unless a subclass documents otherwise, and the
 * library promises no firing order between modules: a consumer must never rely
 * on one event preceding another.</p>
 */
public abstract class DungeonEvent extends Event {

    private final ResourceKey<Level> dimension;

    /**
     * @param dimension the dimension the event happened in; not validated here but
     *     guaranteed non-null by every concrete subclass, each of which rejects a null
     *     argument in its own {@code super(...)} call before any construction state
     *     exists. Subclasses outside this package must do the same.
     */
    protected DungeonEvent(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    /** The dimension the event happened in. */
    public ResourceKey<Level> dimension() {
        return dimension;
    }

    /** The dimension id, for logging and data keys. */
    public String dimensionId() {
        return dimension.location().toString();
    }
}
