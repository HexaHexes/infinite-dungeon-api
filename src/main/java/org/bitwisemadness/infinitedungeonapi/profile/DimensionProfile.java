package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.List;
import java.util.Optional;
import org.bitwisemadness.infinitedungeonapi.data.ResourceRef;

/**
 * A dimension profile: the declaration that a dimension is a dungeon dimension
 * and what its rules are.
 *
 * <p>Written by a datapack at
 * {@code data/<namespace>/infinite_dungeon/dimension_profile/<name>.json} or
 * registered in code through
 * {@link DimensionProfiles#register(DimensionProfile)}. Every field beyond the
 * dimension id is optional: an absent field falls back to the consumer's config
 * default when the profile is resolved with
 * {@link #resolve(DimensionProfileSettings)}.</p>
 *
 * <p>The dimension id stays a plain string so this type is free of Minecraft
 * and Mojang dependencies and fully unit-testable.</p>
 *
 * @param dimension           the dimension id this profile applies to (required)
 * @param layerLimit          the maximum layer index; absent means unbounded
 * @param floorsPerLayer      the floor-count bounds; absent takes the default
 * @param pools               the spawn pool ids
 * @param curves              the per-curve coefficient overrides
 * @param guardian            the layer-guardian policy
 * @param portal              the portal policy
 * @param announcements       the announcement text keys
 * @param spawnSafetyExempt   exemption from the overworld safety gates;
 *                            absent means the dungeon default of {@code true}
 * @param bossRespawn         who gets a defeated boss's fight back (issue
 *                            api#85); absent takes the consumer's default
 */
public record DimensionProfile(String dimension,
                               Optional<Integer> layerLimit,
                               Optional<FloorsPerLayer> floorsPerLayer,
                               Optional<ProfilePools> pools,
                               Optional<CurveOverrides> curves,
                               Optional<GuardianRules> guardian,
                               Optional<PortalPolicy> portal,
                               Optional<ProfileAnnouncements> announcements,
                               Optional<Boolean> spawnSafetyExempt,
                               Optional<BossRespawnPolicy> bossRespawn) {

    public DimensionProfile {
        dimension = dimension == null ? "" : dimension.trim();
        if (dimension.isEmpty()) {
            throw new IllegalArgumentException("dimension is required");
        }
        layerLimit = nonNull(layerLimit);
        floorsPerLayer = nonNull(floorsPerLayer);
        pools = nonNull(pools);
        curves = nonNull(curves);
        guardian = nonNull(guardian);
        portal = nonNull(portal);
        announcements = nonNull(announcements);
        spawnSafetyExempt = nonNull(spawnSafetyExempt);
        bossRespawn = nonNull(bossRespawn);
    }

    /** A profile that only names its dimension; every rule falls to the default. */
    public static DimensionProfile forDimension(String dimension) {
        return new DimensionProfile(dimension,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    /** Whether the dimension id is a well-formed resource location. */
    public boolean hasWellFormedDimension() {
        return ResourceRef.tryParse(dimension).filter(ref -> !ref.tag()).isPresent();
    }

    /** The declared pool ids that are not well-formed resource locations. */
    public List<String> malformedPoolIds() {
        return pools.map(ProfilePools::malformedIds).orElseGet(List::of);
    }

    /** A copy with malformed pool ids dropped. */
    public DimensionProfile sanitised() {
        if (pools.isEmpty()) {
            return this;
        }
        ProfilePools declared = pools.get();
        ProfilePools cleaned = declared.sanitised();
        if (cleaned.equals(declared)) {
            return this;
        }
        return new DimensionProfile(dimension, layerLimit, floorsPerLayer, Optional.of(cleaned),
                curves, guardian, portal, announcements, spawnSafetyExempt, bossRespawn);
    }

    /**
     * Layers this profile over the consumer's config defaults.
     *
     * <p>Precedence is default first, profile second: a declared value always
     * wins, and an absent one falls through. Pools and curves merge per member
     * because each is an independent override; a policy group overrides as a
     * unit.</p>
     */
    public DimensionProfileSettings resolve(DimensionProfileSettings defaults) {
        ProfilePools declaredPools = pools.orElseGet(ProfilePools::empty);
        CurveOverrides declaredCurves = curves.orElseGet(CurveOverrides::empty);
        return new DimensionProfileSettings(
                layerLimit.isPresent() ? layerLimit : defaults.layerLimit(),
                floorsPerLayer.orElse(defaults.floorsPerLayer()),
                new ProfilePools(
                        declaredPools.fodder().or(defaults.pools()::fodder),
                        declaredPools.elite().or(defaults.pools()::elite),
                        declaredPools.boss().or(defaults.pools()::boss)),
                new CurveOverrides(
                        declaredCurves.difficulty().or(defaults.curves()::difficulty),
                        declaredCurves.magicule().or(defaults.curves()::magicule),
                        declaredCurves.density().or(defaults.curves()::density)),
                guardian.orElse(defaults.guardian()),
                portal.orElse(defaults.portal()),
                announcements.orElse(defaults.announcements()),
                spawnSafetyExempt.orElse(defaults.spawnSafetyExempt()),
                bossRespawn.orElse(defaults.bossRespawn()));
    }

    private static <T> Optional<T> nonNull(Optional<T> value) {
        return value == null ? Optional.empty() : value;
    }
}
