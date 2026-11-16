package org.bitwisemadness.infinitedungeonapi.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.infinitedungeonapi.InfiniteDungeonApi;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueCodecs;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueEntry;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueMessage;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueSession;
import org.bitwisemadness.infinitedungeonapi.dialogue.DialogueSessions;
import org.bitwisemadness.infinitedungeonapi.dialogue.Dialogues;

/**
 * Dialogue surface coverage: the codec round trip and the registries.
 *
 * <p>Keep the {@code expectedTests} count in {@code build.gradle} in sync
 * with the total number of {@code @GameTest} methods across this class and
 * {@link ApiSmokeGameTests}, or the harness fails the run.</p>
 */
@GameTestHolder(InfiniteDungeonApi.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DialogueApiGameTests {

    private DialogueApiGameTests() {
    }

    /** The dialogue codec survives the JSON round trip. */
    @GameTest(template = "empty")
    public static void dialogue_codec_roundtrip(GameTestHelper helper) {
        DialogueEntry entry = new DialogueEntry("receptionist", "greeting", "Welcome.");
        JsonElement json = DialogueCodecs.ENTRY.codec().encodeStart(JsonOps.INSTANCE, entry).getOrThrow();
        DialogueEntry restored = DialogueCodecs.ENTRY.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
        helper.assertTrue(restored.npc().equals("receptionist"), "npc did not survive");
        helper.assertTrue(restored.intent().equals("greeting"), "intent did not survive");
        helper.assertTrue(restored.reply().equals("Welcome."), "reply did not survive");
        helper.succeed();
    }

    /** The registry prefers the datapack entry over a code entry. */
    @GameTest(template = "empty")
    public static void dialogue_registry_prefers_datapack(GameTestHelper helper) {
        Dialogues.reset();
        try {
            Dialogues.register("test/greeting", new DialogueEntry("receptionist", "greeting", "code reply"));
            Dialogues.setDatapack(Map.of("test/greeting",
                    new DialogueEntry("receptionist", "greeting", "data reply")));
            helper.assertTrue(Dialogues.get("test/greeting").orElseThrow().reply().equals("data reply"),
                    "datapack entry did not win");
            helper.succeed();
        } finally {
            Dialogues.reset();
        }
    }

    /** The session store holds, loads, and forgets a player's conversation. */
    @GameTest(template = "empty")
    public static void dialogue_session_store_holds_and_forgets(GameTestHelper helper) {
        DialogueSessions.reset();
        try {
            UUID player = UUID.randomUUID();
            DialogueSessions.save(player,
                    new DialogueSession(List.of(new DialogueMessage("user", "hello")), Long.MAX_VALUE));
            helper.assertTrue(DialogueSessions.load(player).isPresent(), "saved session did not load");
            helper.assertTrue(DialogueSessions.load(player).orElseThrow().messages().size() == 1,
                    "saved session lost its message");
            DialogueSessions.forget(player);
            helper.assertTrue(DialogueSessions.load(player).isEmpty(), "forgotten session still loads");
            helper.succeed();
        } finally {
            DialogueSessions.reset();
        }
    }
}
