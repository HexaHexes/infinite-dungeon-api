package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.Objects;

/**
 * A dimension profile's portal rules.
 *
 * @param returnMode            where an exit sends the player
 * @param allowConstruction     whether ignition of new frames is allowed; off
 *                              keeps already-formed portals working
 * @param reformMissingPortal   whether a destroyed anchor frame is re-formed at
 *                              the anchor coordinates
 */
public record PortalPolicy(PortalReturnMode returnMode,
                           boolean allowConstruction,
                           boolean reformMissingPortal) {

    public PortalPolicy {
        Objects.requireNonNull(returnMode, "returnMode");
    }

    /** The shipped defaults: entry-portal return, construction and reform on. */
    public static PortalPolicy defaults() {
        return new PortalPolicy(PortalReturnMode.ENTRY_PORTAL, true, true);
    }
}
