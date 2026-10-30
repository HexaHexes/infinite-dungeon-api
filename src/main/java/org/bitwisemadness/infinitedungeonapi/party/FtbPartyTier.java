package org.bitwisemadness.infinitedungeonapi.party;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

/**
 * The FTB Teams membership tier of the resolver (plan section 8; spike
 * findings on closed core issue #8): parties through FTB Teams' in-jar API.
 * Loaded only behind the facade's {@code ftbteams} gate, so no FTB type ever
 * reaches a class the harness loads without the mod.
 *
 * <p>The guard is the spike's: {@code isManagerLoaded()} must be checked
 * before the manager or it NPEs pre-creation - both documented outcomes fall
 * through to the next tier instead of failing a spawn.</p>
 */
final class FtbPartyTier {

    private FtbPartyTier() {
    }

    /** The player's FTB party, or null to fall through the precedence. */
    static Set<UUID> membersOf(MinecraftServer server, UUID anchor) {
        try {
            var api = FTBTeamsAPI.api();
            if (api == null || !api.isManagerLoaded()) {
                return null;
            }
            var team = api.getManager().getTeamForPlayerID(anchor);
            if (team.isEmpty()) {
                return null;
            }
            Set<UUID> members = new LinkedHashSet<>(team.get().getMembers());
            return members.isEmpty() ? null : members;
        } catch (RuntimeException | LinkageError boundary) {
            // Cross-mod boundary: an unavailable manager falls through.
            return null;
        }
    }
}
