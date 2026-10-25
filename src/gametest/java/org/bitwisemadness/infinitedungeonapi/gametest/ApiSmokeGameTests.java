package org.bitwisemadness.infinitedungeonapi.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.infinitedungeonapi.InfiniteDungeonApi;
import org.bitwisemadness.infinitedungeonapi.profile.CurveCoefficients;
import org.bitwisemadness.infinitedungeonapi.profile.CurveOverrides;
import org.bitwisemadness.infinitedungeonapi.profile.DimensionProfile;
import org.bitwisemadness.infinitedungeonapi.profile.DimensionProfileCodecs;
import org.bitwisemadness.infinitedungeonapi.profile.DimensionProfileSettings;
import org.bitwisemadness.infinitedungeonapi.profile.DimensionProfiles;
import org.bitwisemadness.infinitedungeonapi.profile.FloorsPerLayer;
import org.bitwisemadness.infinitedungeonapi.profile.GuardianRules;
import org.bitwisemadness.infinitedungeonapi.profile.PortalPolicy;
import org.bitwisemadness.infinitedungeonapi.profile.PortalReturnMode;
import org.bitwisemadness.infinitedungeonapi.profile.ProfileAnnouncements;
import org.bitwisemadness.infinitedungeonapi.profile.ProfilePools;

/**
 * Smoke test plus the profile codec's live-server proof.
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

    /**
     * The profile codec round-trips a full document, and a registered profile
     * resolves against config defaults.
     *
     * <p>The round-trip lives here rather than in JUnit because Mojang and Gson
     * stay off the unit-test classpath, so only a live server can exercise a
     * codec.</p>
     */
    @GameTest(template = "empty")
    public static void profile_codec_round_trip_and_resolution(GameTestHelper helper) {
        DimensionProfile profile = new DimensionProfile(
                "infinite_dungeon:dungeon",
                Optional.of(9),
                Optional.of(new FloorsPerLayer(6, 8)),
                Optional.of(new ProfilePools(
                        Optional.of("core:fodder"), Optional.of("core:elite"), Optional.of("core:boss"))),
                Optional.of(new CurveOverrides(
                        Optional.of(new CurveCoefficients(0.0, 0.2, 0.06)),
                        Optional.empty(),
                        Optional.empty())),
                Optional.of(new GuardianRules(120, 15.0)),
                Optional.of(new PortalPolicy(PortalReturnMode.WORLD_SPAWN, false, true)),
                Optional.of(new ProfileAnnouncements("dungeon.entry", "dungeon.layer")),
                Optional.of(false));

        JsonElement json = DimensionProfileCodecs.PROFILE.codec()
                .encodeStart(JsonOps.INSTANCE, profile).getOrThrow();
        DimensionProfile decoded = DimensionProfileCodecs.PROFILE.codec()
                .parse(JsonOps.INSTANCE, json).getOrThrow();
        helper.assertTrue(profile.equals(decoded), "the profile codec must round-trip: " + json);

        DimensionProfileSettings defaults = new DimensionProfileSettings(
                Optional.of(12),
                new FloorsPerLayer(5, 10),
                ProfilePools.empty(),
                CurveOverrides.empty(),
                new GuardianRules(300, 10.0),
                PortalPolicy.defaults(),
                ProfileAnnouncements.empty(),
                true);
        DimensionProfileSettings resolved = profile.resolve(defaults);
        helper.assertTrue(Optional.of(9).equals(resolved.layerLimit()),
                "the declared layer limit must win over the default");
        helper.assertTrue(!resolved.spawnSafetyExempt(),
                "the declared exemption flag must win over the default");
        helper.assertTrue(resolved.curves().magicule().isEmpty(),
                "an undeclared curve stays absent when the default is empty");

        DimensionProfiles.register(profile);
        try {
            helper.assertTrue(DimensionProfiles.isDungeonDimension("infinite_dungeon:dungeon"),
                    "a registered profile must resolve");
            helper.assertTrue(!DimensionProfiles.spawnSafetyExempt("infinite_dungeon:dungeon"),
                    "the registered profile's exemption flag must be visible");
        } finally {
            DimensionProfiles.remove("infinite_dungeon:dungeon");
        }
        helper.succeed();
    }
}
