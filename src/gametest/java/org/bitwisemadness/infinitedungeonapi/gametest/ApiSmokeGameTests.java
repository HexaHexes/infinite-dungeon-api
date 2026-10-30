package org.bitwisemadness.infinitedungeonapi.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.infinitedungeonapi.InfiniteDungeonApi;
import org.bitwisemadness.infinitedungeonapi.event.BossSlainEvent;
import org.bitwisemadness.infinitedungeonapi.event.FloorEnteredEvent;
import org.bitwisemadness.infinitedungeonapi.event.GateUnlockedEvent;
import org.bitwisemadness.infinitedungeonapi.event.LayerCompletedEvent;
import org.bitwisemadness.infinitedungeonapi.event.LayerEnteredEvent;
import org.bitwisemadness.infinitedungeonapi.event.SpawnGateDecisionEvent;
import org.bitwisemadness.infinitedungeonapi.party.PartySizeResolver;
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
import org.bitwisemadness.infinitedungeonapi.spawn.SpawnCategory;
import org.bitwisemadness.infinitedungeonapi.stats.AttributeScaling;

/**
 * Smoke test plus the profile codec's and lifecycle events' live-server proof.
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

    /**
     * Every lifecycle event fires on the game bus and carries its payload, and
     * the one cancellable event keeps a listener's veto.
     */
    @GameTest(template = "empty")
    public static void lifecycle_events_fire_and_carry_their_payload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ResourceKey<Level> dimension = level.dimension();
        UUID playerId = UUID.randomUUID();
        Mob boss = EntityType.ZOMBIE.create(level);
        helper.assertTrue(boss != null, "the boss must be creatable");

        ObservingListener listener = new ObservingListener();
        NeoForge.EVENT_BUS.register(listener);
        try {
            NeoForge.EVENT_BUS.post(new LayerEnteredEvent(dimension, playerId, 3));
            NeoForge.EVENT_BUS.post(new FloorEnteredEvent(dimension, playerId, 3, 7));
            NeoForge.EVENT_BUS.post(new LayerCompletedEvent(dimension, 3));
            NeoForge.EVENT_BUS.post(new BossSlainEvent(dimension, 3, boss));
            NeoForge.EVENT_BUS.post(new GateUnlockedEvent(dimension, "infinite_dungeon:layer_3_gate"));

            // A pack override cancels from inside a listener; the posted event
            // comes back carrying the veto.
            SpawnGateDecisionEvent decision = NeoForge.EVENT_BUS.post(new SpawnGateDecisionEvent(
                    dimension, boss, SpawnCategory.HOSTILE,
                    helper.absolutePos(BlockPos.ZERO), 0.0, 50.0));

            helper.assertTrue(listener.layerEntered == 3, "LayerEnteredEvent must carry the layer");
            helper.assertTrue(listener.floorEntered == 7, "FloorEnteredEvent must carry the floor");
            helper.assertTrue(listener.layerCompleted, "LayerCompletedEvent must fire");
            helper.assertTrue(listener.bossSlain, "BossSlainEvent must carry the boss");
            helper.assertTrue("infinite_dungeon:layer_3_gate".equals(listener.gateId),
                    "GateUnlockedEvent must carry the gate id");
            helper.assertTrue(listener.decisionSeen,
                    "the gate decision must reach a listener");
            helper.assertTrue(decision.isCanceled(),
                    "a listener's veto must survive the post");
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    /**
     * The attribute scaling list (issue api#67): the curve rule against the
     * jar-verified Tensura names, registration and enablement, the plan's
     * magicule toggle semantics (off = scale as DIFFICULTY, never drop), and
     * the no-Tensura enumeration being a no-op. Static state resets in
     * finally - the batch shares the registry.
     */
    @GameTest(template = "empty")
    public static void attribute_scaling_list_curves_and_toggle(GameTestHelper helper) {
        try {
            // The name rule, pinned against the pinned jar's ids:
            // magicule-capacity attributes key M; the magic_* defense family
            // and everything else keys D.
            helper.assertTrue(AttributeScaling.curveFor("max_magicule")
                            == AttributeScaling.Curve.MAGICULE,
                    "max_magicule is magicule-specific");
            helper.assertTrue(AttributeScaling.curveFor("magicule_regeneration_multiplier")
                            == AttributeScaling.Curve.MAGICULE,
                    "regeneration is magicule-specific");
            helper.assertTrue(AttributeScaling.curveFor("max_aura")
                            == AttributeScaling.Curve.DIFFICULTY,
                    "aura keys difficulty");
            helper.assertTrue(AttributeScaling.curveFor("magic_barrier")
                            == AttributeScaling.Curve.DIFFICULTY,
                    "the magic_* defense family is not magicule-specific");

            ResourceLocation plain = ResourceLocation.parse("infinite_dungeon_api:smoke_plain");
            ResourceLocation magicule = ResourceLocation.parse("infinite_dungeon_api:smoke_magicule");
            AttributeScaling.register(plain, AttributeScaling.Curve.DIFFICULTY);
            AttributeScaling.register(magicule, AttributeScaling.Curve.MAGICULE);
            helper.assertTrue(AttributeScaling.entries().stream().anyMatch(
                            row -> row.attribute().equals(plain)
                                    && row.curve() == AttributeScaling.Curve.DIFFICULTY),
                    "a registered row must appear with its curve");
            helper.assertTrue(AttributeScaling.entries().stream().anyMatch(
                            row -> row.attribute().equals(magicule)
                                    && row.curve() == AttributeScaling.Curve.MAGICULE),
                    "with the plan-default toggle on, M-keying stands");

            AttributeScaling.setEnabled(magicule, false);
            helper.assertTrue(AttributeScaling.entries().stream().noneMatch(
                            row -> row.attribute().equals(magicule)),
                    "a disabled row must leave the effective list");
            AttributeScaling.setEnabled(magicule, true);

            AttributeScaling.setMagiculeKeyedScaling(false);
            helper.assertTrue(AttributeScaling.entries().stream().anyMatch(
                            row -> row.attribute().equals(magicule)
                                    && row.curve() == AttributeScaling.Curve.DIFFICULTY),
                    "toggle off: still included, but scaling as DIFFICULTY");
            AttributeScaling.setMagiculeKeyedScaling(true);

            int before = AttributeScaling.entries().size();
            AttributeScaling.registerTensuraAttributes();
            helper.assertTrue(AttributeScaling.entries().size() == before,
                    "without Tensura loaded the enumeration registers nothing and crashes nowhere");
        } finally {
            AttributeScaling.reset();
        }
        helper.succeed();
    }

    /**
     * The party resolver's presence semantics (plan section 8, issue api#65):
     * nobody near = 1.0, an unteamed player is a solo party, players outside
     * the radius never count, and offline (ghost) team names never count -
     * every path here must read exactly 1.0; JUnit pins the greater-than-one
     * arithmetic, and the mod tiers are compile-gated paths verified at
     * wiring. Team state is world-persistent, so the team is looked up
     * before creation (the harness world outlives runs).
     */
    @GameTest(template = "empty")
    public static void party_size_resolver_reads_presence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos probe = helper.absolutePos(new BlockPos(0, 4, 0));
        double radius = 8.0;
        double perMember = 0.5;
        helper.assertTrue(PartySizeResolver.multiplier(level, probe, radius, perMember) == 1.0,
                "with nobody near the spawn the multiplier must be 1.0");

        ServerPlayer near = helper.makeMockServerPlayerInLevel();
        near.setPos(probe.getX() + 2.0, probe.getY(), probe.getZ());
        helper.assertTrue(PartySizeResolver.multiplier(level, probe, radius, perMember) == 1.0,
                "an unteamed player near the spawn is a solo party");

        ServerPlayer beyond = helper.makeMockServerPlayerInLevel();
        beyond.setPos(probe.getX() + 40.0, probe.getY(), probe.getZ());
        helper.assertTrue(PartySizeResolver.multiplier(level, probe, radius, perMember) == 1.0,
                "players outside the presence radius never count");

        // A team carrying an offline (ghost) name: the online-only rule keeps
        // the party solo - a ghost must never inflate a spawn.
        PlayerTeam team = level.getScoreboard().getPlayerTeam("resolver_probe");
        if (team == null) {
            team = level.getScoreboard().addPlayerTeam("resolver_probe");
        }
        level.getScoreboard().addPlayerToTeam(near.getGameProfile().getName(), team);
        level.getScoreboard().addPlayerToTeam("resolver_ghost_member", team);
        helper.assertTrue(PartySizeResolver.multiplier(level, probe, radius, perMember) == 1.0,
                "offline team members never count (online-only presence)");
        helper.succeed();
    }

    /** Records what the six events delivered; public so the bus can invoke it. */
    public static final class ObservingListener {

        private volatile int layerEntered = -1;
        private volatile int floorEntered = -1;
        private volatile boolean layerCompleted;
        private volatile boolean bossSlain;
        private volatile String gateId;
        private volatile boolean decisionSeen;

        @SubscribeEvent
        public void onLayerEntered(LayerEnteredEvent event) {
            layerEntered = event.layer();
        }

        @SubscribeEvent
        public void onFloorEntered(FloorEnteredEvent event) {
            floorEntered = event.floor();
        }

        @SubscribeEvent
        public void onLayerCompleted(LayerCompletedEvent event) {
            layerCompleted = true;
        }

        @SubscribeEvent
        public void onBossSlain(BossSlainEvent event) {
            bossSlain = event.boss() != null;
        }

        @SubscribeEvent
        public void onGateUnlocked(GateUnlockedEvent event) {
            gateId = event.gateId();
        }

        @SubscribeEvent
        public void onGateDecision(SpawnGateDecisionEvent event) {
            decisionSeen = true;
            // Stand in for a pack override: veto the block.
            event.setCanceled(true);
        }
    }
}
