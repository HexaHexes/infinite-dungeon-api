package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Optional;

/**
 * The entrance location-provider seam (api#58): answers where an entrance may
 * generate, or empty when the position is not this provider's to answer. The
 * guardian-presence seam's shape (the register's provider-seam precedent):
 * implementations must be server-side, must not block, and must be safe to
 * call during worldgen. The API owns the generation policy; a provider never
 * decides policy, only suitability and location.
 */
@FunctionalInterface
public interface EntranceLocationProvider {

    /** The entrance site for the query, or empty when this provider has no answer. */
    Optional<EntranceSite> locate(EntranceContext context);
}
