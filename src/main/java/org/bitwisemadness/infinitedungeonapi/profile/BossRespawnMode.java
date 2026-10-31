package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Locale;

/** How a defeated boss or guardian comes back (issue api#85, D39's vocabulary). */
public enum BossRespawnMode {

    /** Every player who has not had their fight yet gets one (the default). */
    PER_PLAYER,

    /** One defeat per world; nothing spawns afterwards. */
    ONCE,

    /** Respawns after the policy's tick count once no player is inside. */
    TIMER;

    /** Parses a JSON or config value, falling back to {@link #PER_PLAYER}. */
    public static BossRespawnMode parse(String raw) {
        if (raw == null) {
            return PER_PLAYER;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "once" -> ONCE;
            case "timer" -> TIMER;
            default -> PER_PLAYER;
        };
    }

    /** The canonical lowercase name used in JSON and config. */
    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
