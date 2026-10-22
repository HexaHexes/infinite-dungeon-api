package org.bitwisemadness.infinitedungeonapi.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.infinitedungeonapi.InfiniteDungeonApi;

/**
 * Smoke test: proves the library loads and the GameTest harness runs.
 *
 * <p>Keep the {@code expectedTests} count in {@code build.gradle} in sync with
 * the number of {@code @GameTest} methods here, or the harness fails the
 * run.</p>
 */
@GameTestHolder(InfiniteDungeonApi.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ApiSmokeGameTests {

    private ApiSmokeGameTests() {
    }

    /** The library is loaded and the empty structure template resolves. */
    @GameTest(template = "empty")
    public static void api_loads(GameTestHelper helper) {
        helper.succeed();
    }
}
