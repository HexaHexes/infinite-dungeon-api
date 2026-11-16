package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.Objects;

/** One message in a conversation: the role and the content. */
public record DialogueMessage(String role, String content) {

    public DialogueMessage {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(content, "content");
    }
}