package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Objects;
import java.util.Optional;

/**
 * The effective settings a dimension runs under, after a profile is resolved
 * against the consumer's config defaults.
 *
 * <p>This is the shape consumers read and construct: the core module builds one
 * from its config and passes it to {@link DimensionProfile#resolve}, and the
 * result has the same shape with every profile value layered on top. A field
 * that neither the profile nor the defaults supply stays absent, and the
 * consumer keeps its own behaviour for it.</p>
 *
 * @param layerLimit          the maximum layer index; absent means unbounded
 * @param floorsPerLayer      the floor-count bounds a layer draws from
 * @param pools               the spawn pool ids
 * @param curves              the per-curve coefficient overrides
 * @param guardian            the layer-guardian policy
 * @param portal              the portal policy
 * @param announcements       the announcement text keys
 * @param spawnSafetyExempt   whether the dimension is exempt from the overworld
 *                            safety gates
 * @param bossRespawn         who gets a defeated boss's fight back (issue
 *                            api#85); always resolved, never null
 */
public record DimensionProfileSettings(Optional<Integer> layerLimit,
                                       FloorsPerLayer floorsPerLayer,
                                       ProfilePools pools,
                                       CurveOverrides curves,
                                       GuardianRules guardian,
                                       PortalPolicy portal,
                                       ProfileAnnouncements announcements,
                                       boolean spawnSafetyExempt,
                                       BossRespawnPolicy bossRespawn) {

    public DimensionProfileSettings {
        layerLimit = layerLimit == null ? Optional.empty() : layerLimit;
        Objects.requireNonNull(floorsPerLayer, "floorsPerLayer");
        Objects.requireNonNull(pools, "pools");
        Objects.requireNonNull(curves, "curves");
        Objects.requireNonNull(guardian, "guardian");
        Objects.requireNonNull(portal, "portal");
        Objects.requireNonNull(announcements, "announcements");
        Objects.requireNonNull(bossRespawn, "bossRespawn");
    }
}
