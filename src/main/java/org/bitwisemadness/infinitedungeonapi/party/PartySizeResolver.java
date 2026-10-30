package org.bitwisemadness.infinitedungeonapi.party;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.neoforged.fml.ModList;

/**
 * The party multiplier resolver (plan section 8, issue api#65): how strong a
 * spawn scales when a party fights it, with the record's fixed precedence -
 * OPAC, then FTB Teams, then vanilla teams.
 *
 * <p>Semantics pinned before implementation (the design comment on the
 * issue): the radius is a <em>presence</em> radius around the spawn - only
 * ONLINE members inside it count, so absent members never inflate a spawn;
 * the anchor is the nearest online player inside the radius (their party is
 * the fight's party); nobody nearby means a solo spawn of exactly 1.0.
 * Precedence resolves HOW a party is found, never HOW it is counted - all
 * three tiers feed the same online-distance tally. The multiplier itself is
 * the pure {@link PartyMultiplier#of} arithmetic; callers pass the configured
 * radius and per-member value (their config, the TravelPolicy rule - the
 * {@code party.*} keys ship with core #37, the consumer).</p>
 *
 * <p>The mod tiers live in their own classes behind {@code ModList} gates, so
 * this class and its vanilla-tier path load and run on a classpath with
 * neither mod installed - the structural form of the lesson core #200's gate
 * taught.</p>
 */
public final class PartySizeResolver {

    private PartySizeResolver() {
    }

    /**
     * The party multiplier for a spawn at the position.
     *
     * @param level the level the spawn happens in (players are read from it)
     * @param spawnPos the spawn position the radius measures around
     * @param radius the presence radius in blocks (config: party radius)
     * @param perMember the per-member increment (config: party per-member)
     * @return 1.0 when no party member is present; otherwise
     *         {@link PartyMultiplier#of} over the present members
     */
    public static double multiplier(ServerLevel level, BlockPos spawnPos,
            double radius, double perMember) {
        ServerPlayer anchor = nearestPresent(level, spawnPos, radius);
        if (anchor == null) {
            return 1.0;
        }
        Set<UUID> party = resolveParty(level, anchor);
        return PartyMultiplier.of(countPresent(level, spawnPos, radius, party), perMember);
    }

    /** The nearest online player inside the radius, or null (a solo spawn). */
    private static ServerPlayer nearestPresent(ServerLevel level, BlockPos spawnPos, double radius) {
        double radiusSq = radius * radius;
        ServerPlayer nearest = null;
        double nearestSq = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            double distanceSq = player.distanceToSqr(
                    spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            if (distanceSq <= radiusSq && distanceSq < nearestSq) {
                nearest = player;
                nearestSq = distanceSq;
            }
        }
        return nearest;
    }

    /**
     * The anchor's party by fixed precedence - OPAC, FTB Teams, vanilla teams
     * - always including the anchor itself (an unteamed player is their own
     * party of one).
     */
    private static Set<UUID> resolveParty(ServerLevel level, ServerPlayer anchor) {
        Set<UUID> party = null;
        if (ModList.get().isLoaded("openpartiesandclaims")) {
            party = OpacTier.membersOf(level.getServer(), anchor.getUUID());
        }
        if (party == null && ModList.get().isLoaded("ftbteams")) {
            party = FtbPartyTier.membersOf(level.getServer(), anchor.getUUID());
        }
        if (party == null) {
            party = vanillaTeamMembers(anchor);
        }
        return party;
    }

    /** Vanilla teams by scoreboard name - online members only. */
    private static Set<UUID> vanillaTeamMembers(ServerPlayer anchor) {
        Set<UUID> members = new LinkedHashSet<>();
        members.add(anchor.getUUID());
        Team team = anchor.getTeam();
        if (team == null) {
            return members;
        }
        for (String name : team.getPlayers()) {
            ServerPlayer member = anchor.getServer().getPlayerList().getPlayerByName(name);
            if (member != null) {
                members.add(member.getUUID());
            }
        }
        return members;
    }

    /** How many party members are online inside the radius of the spawn (anchor counted). */
    private static int countPresent(ServerLevel level, BlockPos spawnPos,
            double radius, Set<UUID> party) {
        double radiusSq = radius * radius;
        int present = 0;
        for (UUID id : party) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
            if (member == null) {
                continue;
            }
            if (member.distanceToSqr(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()) <= radiusSq) {
                present++;
            }
        }
        return Math.max(present, 1);
    }
}
