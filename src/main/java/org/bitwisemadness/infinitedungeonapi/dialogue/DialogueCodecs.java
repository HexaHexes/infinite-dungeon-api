package org.bitwisemadness.infinitedungeonapi.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * JSON codecs for dialogue entries.
 *
 * <p>Separate from the value types so those stay free of Mojang dependencies
 * and unit-testable without a game runtime, following the data layer's rule.
 * The codec lives on the game-facing side: the reload listener is its only
 * production caller, and the GameTest is its round-trip proof.</p>
 */
public final class DialogueCodecs {

    private DialogueCodecs() {
    }

    /** The whole dialogue entry document. */
    public static final MapCodec<DialogueEntry> ENTRY = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("npc").forGetter(DialogueEntry::npc),
            Codec.STRING.fieldOf("intent").forGetter(DialogueEntry::intent),
            Codec.STRING.fieldOf("reply").forGetter(DialogueEntry::reply)
    ).apply(instance, DialogueEntry::new));
}