package org.bitwisemadness.infinitedungeonapi.party;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.api.OpenPACServerAPI;

/**
 * The OPAC membership tier of the resolver (plan section 8; spike findings on
 * issue api#63): parties through Open Parties and Claims. Loaded only behind
 * the facade's {@code openpartiesandclaims} gate, so no OPAC type ever reaches
 * a class the harness loads without the mod.
 *
 * <p>Access path (verified against the pinned jar): {@code
 * OpenPACServerAPI.get(server).getPartyManager().getPartyByMember(uuid)} then
 * the member stream's UUIDs. Returns null - the caller falls through to the
 * next tier - when the player has no party, no party system answers, or the
 * boundary throws (a facade that can NPE before its systems boot is exactly
 * what the FTB spike documented for its sibling).</p>
 */
final class OpacTier {

    private OpacTier() {
    }

    /** The player's OPAC party, or null to fall through the precedence. */
    static Set<UUID> membersOf(MinecraftServer server, UUID anchor) {
        try {
            // get() is non-null by construction (SpotBugs' redundant-nullcheck
            // finding proved it against the jar); an unavailable facade still
            // throws into the boundary below and falls through.
            var party = OpenPACServerAPI.get(server).getPartyManager().getPartyByMember(anchor);
            if (party == null) {
                return null;
            }
            Set<UUID> members = new LinkedHashSet<>();
            party.getMemberInfoStream().forEach(member -> members.add(member.getUUID()));
            return members.isEmpty() ? null : members;
        } catch (RuntimeException | LinkageError boundary) {
            // Cross-mod boundary: an unavailable facade falls through to the
            // next tier rather than failing the spawn.
            return null;
        }
    }
}
