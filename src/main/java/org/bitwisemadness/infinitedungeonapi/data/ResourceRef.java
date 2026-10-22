package org.bitwisemadness.infinitedungeonapi.data;

import java.util.Optional;

/**
 * A reference to a registry entry or a tag, written as {@code ns:path} or
 * {@code #ns:path}.
 *
 * <p>The location stays a plain string and this class touches no Minecraft or
 * Mojang types on purpose: the data layer is pure logic that must be
 * unit-testable without a Minecraft runtime. Consumers convert to
 * {@code ResourceLocation} at the boundary where they touch the game, and the
 * JSON codecs live in {@link InfiniteDungeonCodecs}.</p>
 *
 * @param location the namespaced location, without the tag prefix
 * @param tag      whether this reference points at a tag
 */
public record ResourceRef(String location, boolean tag) {

    public static final char TAG_PREFIX = '#';

    /** Parses a reference, throwing when it is malformed. */
    public static ResourceRef parse(String raw) {
        return tryParse(raw).orElseThrow(
                () -> new IllegalArgumentException("Not a valid resource reference: " + raw));
    }

    /** Parses a reference, returning empty when it is malformed. */
    public static Optional<ResourceRef> tryParse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        boolean tag = trimmed.startsWith(String.valueOf(TAG_PREFIX));
        String location = tag ? trimmed.substring(1) : trimmed;
        if (!isValidLocation(location)) {
            return Optional.empty();
        }
        return Optional.of(new ResourceRef(location, tag));
    }

    /** Serializes back to the {@code ns:path} / {@code #ns:path} form. */
    public String serialize() {
        return tag ? TAG_PREFIX + location : location;
    }

    /**
     * Validates the Minecraft resource location shape: an optional single
     * namespace, lowercase letters, digits, and {@code _ - . /}.
     */
    public static boolean isValidLocation(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        int colon = location.indexOf(':');
        if (colon != location.lastIndexOf(':')) {
            return false;
        }
        if (colon == 0 || colon == location.length() - 1) {
            return false;
        }
        for (int i = 0; i < location.length(); i++) {
            char c = location.charAt(i);
            boolean legal = c == ':' || c == '_' || c == '-' || c == '.' || c == '/'
                    || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
            if (!legal) {
                return false;
            }
        }
        return true;
    }
}
