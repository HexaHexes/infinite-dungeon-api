package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.Optional;
import java.util.UUID;

/**
 * A conversation-state store keyed by player.
 *
 * <p>Implementations must be server-side and must not block. The default
 * in-memory store in {@link DialogueSessions} expires sessions on load; a
 * registered store replaces it.</p>
 */
public interface DialogueSessionStore {

    /** The player's session, or empty when absent or expired. */
    Optional<DialogueSession> load(UUID playerId);

    /** Stores the player's session, replacing any earlier one. */
    void save(UUID playerId, DialogueSession session);

    /** Drops the player's session. */
    void forget(UUID playerId);
}
