package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The suite's dialogue session service: one store behind a static registry.
 *
 * <p>The default is an in-memory store that expires sessions on load. A
 * consumer can register its own store - persistent or distributed - and the
 * whole suite routes through it. With no stored session the answer is empty,
 * and every consumer treats that as a fresh conversation.</p>
 */
public final class DialogueSessions {

    /** The default session lifetime in milliseconds. */
    public static final long DEFAULT_TTL_MILLIS = 600_000L;

    private static volatile DialogueSessionStore store = new InMemoryStore();

    private DialogueSessions() {
    }

    /** Registers the store that serves every session query. */
    public static void register(DialogueSessionStore impl) {
        store = Objects.requireNonNull(impl, "impl");
    }

    /** Restores the default in-memory store. */
    public static void unregister() {
        store = new InMemoryStore();
    }

    /** The player's session, or empty when absent or expired. */
    public static Optional<DialogueSession> load(UUID playerId) {
        return store.load(playerId);
    }

    /** Stores the player's session, replacing any earlier one. */
    public static void save(UUID playerId, DialogueSession session) {
        store.save(playerId, session);
    }

    /** Drops the player's session. */
    public static void forget(UUID playerId) {
        store.forget(playerId);
    }

    /** The registered store. */
    public static DialogueSessionStore store() {
        return store;
    }

    /** Clears the store; used by reload teardown and tests. */
    public static void reset() {
        store = new InMemoryStore();
    }

    private static final class InMemoryStore implements DialogueSessionStore {
        private final Map<UUID, DialogueSession> sessions = new ConcurrentHashMap<>();

        @Override
        public Optional<DialogueSession> load(UUID playerId) {
            DialogueSession session = sessions.get(playerId);
            if (session == null) {
                return Optional.empty();
            }
            if (session.isExpired(System.nanoTime())) {
                sessions.remove(playerId);
                return Optional.empty();
            }
            return Optional.of(session);
        }

        @Override
        public void save(UUID playerId, DialogueSession session) {
            sessions.put(playerId, Objects.requireNonNull(session, "session"));
        }

        @Override
        public void forget(UUID playerId) {
            sessions.remove(playerId);
        }
    }
}