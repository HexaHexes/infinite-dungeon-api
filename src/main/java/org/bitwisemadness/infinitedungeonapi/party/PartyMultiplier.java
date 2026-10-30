package org.bitwisemadness.infinitedungeonapi.party;

/**
 * The party multiplier's pure arithmetic (plan section 8, issue api#65) -
 * kept Minecraft-free so the unit suite loads without a Minecraft runtime
 * (the standing rule the particle split made structural).
 *
 * <p>One formula, one job: {@code 1 + (present - 1) * perMember} - a solo
 * fighter is exactly 1.0, and every ADDITIONAL present member adds
 * {@code perMember} times the base strength. The record pins the knob names
 * (radius, per-member multiplier) but not the formula; this is the natural
 * reading of "per-member multiplier" and is trivially re-tunable
 * pre-publication.</p>
 */
public final class PartyMultiplier {

    private PartyMultiplier() {
    }

    /**
     * The multiplier for a party of {@code present} members.
     *
     * @param present how many members are present at the spawn (at least 1 -
     *                the anchor counts itself)
     * @param perMember the configured per-member increment
     * @return 1.0 for a solo present member, growing per additional member
     * @throws IllegalArgumentException when present is negative
     */
    public static double of(int present, double perMember) {
        if (present < 0) {
            throw new IllegalArgumentException("present must be non-negative, got " + present);
        }
        return 1.0 + Math.max(present - 1, 0) * perMember;
    }
}
