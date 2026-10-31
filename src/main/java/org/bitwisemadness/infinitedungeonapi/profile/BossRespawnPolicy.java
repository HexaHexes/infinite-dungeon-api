package org.bitwisemadness.infinitedungeonapi.profile;

/**
 * A dimension's respawn policy for bosses and guardians (issue api#85, after
 * the D39 comparison decision): <b>who</b> gets the fight back and, for the
 * timed mode, after how long.
 *
 * <p>The tick count is carried in every mode so switching modes needs no
 * schema change; it is only <em>read</em> for {@link BossRespawnMode#TIMER}.
 * The default pairs {@link BossRespawnMode#PER_PLAYER} with 12000 ticks -
 * 600 seconds, the cooldown the suite already ships - so an absent policy
 * changes no behaviour anywhere.</p>
 *
 * @param mode          who gets the fight back
 * @param respawnTicks  the timer mode's delay; must be positive
 */
public record BossRespawnPolicy(BossRespawnMode mode, long respawnTicks) {

    /** The default timer delay: 600 seconds, matching the shipped cooldown. */
    public static final long DEFAULT_RESPAWN_TICKS = 12000L;

    public BossRespawnPolicy {
        if (mode == null) {
            throw new IllegalArgumentException("boss respawn mode is required");
        }
        if (respawnTicks <= 0) {
            throw new IllegalArgumentException(
                    "boss respawnTicks must be positive; got " + respawnTicks);
        }
    }

    /** Per-player fights on the shipped 600-second timer delay. */
    public static BossRespawnPolicy defaults() {
        return new BossRespawnPolicy(BossRespawnMode.PER_PLAYER, DEFAULT_RESPAWN_TICKS);
    }
}
