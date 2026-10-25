package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Locale;

/** How a dungeon exit picks its destination. */
public enum PortalReturnMode {

    /** Return through the anchor portal the player entered by (the default). */
    ENTRY_PORTAL,

    /** Return to the overworld world spawn. */
    WORLD_SPAWN;

    /** Parses a JSON or config value, falling back to {@link #ENTRY_PORTAL}. */
    public static PortalReturnMode parse(String raw) {
        if (raw == null) {
            return ENTRY_PORTAL;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "world_spawn" -> WORLD_SPAWN;
            default -> ENTRY_PORTAL;
        };
    }

    /** The canonical lowercase name used in JSON and config. */
    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
