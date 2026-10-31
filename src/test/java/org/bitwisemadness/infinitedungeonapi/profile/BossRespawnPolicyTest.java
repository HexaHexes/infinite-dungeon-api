package org.bitwisemadness.infinitedungeonapi.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The respawn policy (issue api#85): parsing, validation, defaults. */
class BossRespawnPolicyTest {

    @Test
    void modesParseFromTheirJsonNames() {
        assertEquals(BossRespawnMode.PER_PLAYER, BossRespawnMode.parse("per_player"));
        assertEquals(BossRespawnMode.ONCE, BossRespawnMode.parse("once"));
        assertEquals(BossRespawnMode.TIMER, BossRespawnMode.parse(" TIMER "));
        assertEquals(BossRespawnMode.PER_PLAYER, BossRespawnMode.parse("unknown-value"),
                "an unknown mode falls back to per_player");
        assertEquals(BossRespawnMode.PER_PLAYER, BossRespawnMode.parse(null),
                "a missing mode falls back to per_player");
        assertEquals("per_player", BossRespawnMode.PER_PLAYER.serializedName());
        assertEquals("timer", BossRespawnMode.TIMER.serializedName());
    }

    @Test
    void defaultsPairPerPlayerWithTheShippedCooldown() {
        BossRespawnPolicy defaults = BossRespawnPolicy.defaults();
        assertEquals(BossRespawnMode.PER_PLAYER, defaults.mode(),
                "the default mode must keep shipped behaviour");
        assertEquals(12000L, defaults.respawnTicks(),
                "the default delay must match the shipped 600-second cooldown");
    }

    @Test
    void invalidDeclarationsNameTheOffendingValue() {
        var nullMode = assertThrows(IllegalArgumentException.class,
                () -> new BossRespawnPolicy(null, 100));
        assertTrue(nullMode.getMessage().contains("mode is required"), nullMode.getMessage());
        var badTicks = assertThrows(IllegalArgumentException.class,
                () -> new BossRespawnPolicy(BossRespawnMode.TIMER, 0));
        assertTrue(badTicks.getMessage().contains("got 0"), badTicks.getMessage());
        var negativeTicks = assertThrows(IllegalArgumentException.class,
                () -> new BossRespawnPolicy(BossRespawnMode.TIMER, -5));
        assertTrue(negativeTicks.getMessage().contains("got -5"), negativeTicks.getMessage());
    }
}
