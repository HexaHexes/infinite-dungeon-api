package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A player's conversation state: the ordered messages and the expiry
 * deadline. Immutable; appending returns a new session.
 */
public record DialogueSession(List<DialogueMessage> messages, long expiresAtNanos) {

    public DialogueSession {
        Objects.requireNonNull(messages, "messages");
        if (expiresAtNanos < 0) {
            throw new IllegalArgumentException("expiresAtNanos must not be negative");
        }
    }

    /** A new session expiring ttlMillis from now. */
    public static DialogueSession withTtl(List<DialogueMessage> messages, long ttlMillis) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be positive");
        }
        return new DialogueSession(messages, System.nanoTime() + ttlMillis * 1_000_000L);
    }

    /** Whether the session has expired at the given instant. */
    public boolean isExpired(long nowNanos) {
        return nowNanos > expiresAtNanos;
    }

    /** A copy of this session with the message appended. */
    public DialogueSession append(DialogueMessage message) {
        Objects.requireNonNull(message, "message");
        List<DialogueMessage> next = new ArrayList<>(messages.size() + 1);
        next.addAll(messages);
        next.add(message);
        return new DialogueSession(Collections.unmodifiableList(next), expiresAtNanos);
    }
}