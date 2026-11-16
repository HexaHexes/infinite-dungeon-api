package org.bitwisemadness.infinitedungeonapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueMessage;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueSession;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueSessions;
import org.junit.jupiter.api.Test;

/** Pure-JVM checks on the dialogue session model and the in-memory store. */
class DialogueSessionTest {

    @Test
    void sessionAppendsMessages() {
        DialogueSession session = new DialogueSession(List.of(), Long.MAX_VALUE);
        DialogueSession next = session.append(new DialogueMessage("user", "hello"));
        assertEquals(1, next.messages().size());
        assertEquals("user", next.messages().get(0).role());
        assertEquals("hello", next.messages().get(0).content());
        assertEquals(0, session.messages().size());
    }

    @Test
    void sessionExpires() {
        long now = System.nanoTime();
        DialogueSession expired = new DialogueSession(List.of(), now - 1);
        DialogueSession alive = new DialogueSession(List.of(), now + 1_000_000L);
        assertTrue(expired.isExpired(now));
        assertFalse(alive.isExpired(now));
    }

    @Test
    void registryStoresLoadsAndForgets() {
        UUID player = UUID.randomUUID();
        DialogueSessions.reset();
        assertTrue(DialogueSessions.load(player).isEmpty());
        DialogueSessions.save(player,
                new DialogueSession(List.of(new DialogueMessage("assistant", "welcome")), Long.MAX_VALUE));
        assertEquals("welcome", DialogueSessions.load(player).orElseThrow().messages().get(0).content());
        DialogueSessions.forget(player);
        assertTrue(DialogueSessions.load(player).isEmpty());
    }

    @Test
    void registryExpiresStoredSessions() {
        UUID player = UUID.randomUUID();
        DialogueSessions.reset();
        DialogueSessions.save(player, new DialogueSession(List.of(), System.nanoTime() - 1));
        assertTrue(DialogueSessions.load(player).isEmpty());
    }

    @Test
    void registryRejectsInvalidArguments() {
        DialogueSessions.reset();
        assertThrows(NullPointerException.class, () -> DialogueSessions.register(null));
        assertThrows(NullPointerException.class, () -> new DialogueSession(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new DialogueSession(List.of(), -1));
        assertThrows(IllegalArgumentException.class, () -> DialogueSession.withTtl(List.of(), 0));
    }
}