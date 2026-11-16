package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.Objects;

/**
 * A datapack dialogue entry: the NPC, the intent it answers, and the reply.
 */
public record DialogueEntry(String npc, String intent, String reply) {

    public DialogueEntry {
        Objects.requireNonNull(npc, "npc");
        Objects.requireNonNull(intent, "intent");
        Objects.requireNonNull(reply, "reply");
    }
}
