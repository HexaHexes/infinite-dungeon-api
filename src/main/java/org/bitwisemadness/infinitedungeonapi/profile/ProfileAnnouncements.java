package org.bitwisemadness.infinitedungeonapi.profile;

/**
 * A dimension profile's announcement text keys.
 *
 * <p>Keys, not literals: the consumer resolves them against its own language
 * file, so a pack can ship translations without touching data.</p>
 *
 * @param entry the key announced on dimension entry
 * @param layer the key announced on layer entry
 */
public record ProfileAnnouncements(String entry, String layer) {

    public ProfileAnnouncements {
        entry = entry == null ? "" : entry;
        layer = layer == null ? "" : layer;
    }

    /** Announcements that name no key. */
    public static ProfileAnnouncements empty() {
        return new ProfileAnnouncements("", "");
    }
}
