package org.bitwisemadness.infinitedungeonapi.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import org.bitwisemadness.infinitedungeonapi.event.MinibossSlainEvent;
import org.bitwisemadness.infinitedungeonapi.event.SpawnGateDecisionEvent;
import org.bitwisemadness.infinitedungeonapi.magicule.AmbientMagicule;
import org.bitwisemadness.infinitedungeonapi.magicule.MagiculeProvider;
import org.bitwisemadness.infinitedungeonapi.magicule.TensuraMagiculeBridge;
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
import org.bitwisemadness.infinitedungeonapi.util.Determinism;

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
     * The additive attribution payloads (api#53, ADR-0003): attributed
     * construction carries the player id, the legacy constructors and a null
     * attribution carry the documented unknown rather than throwing, and
     * every variant survives the bus.
     */
    @GameTest(template = "empty")
    public static void attribution_payloads_follow_adr_0003(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ResourceKey<Level> dimension = level.dimension();
        UUID killer = UUID.randomUUID();
        Mob boss = EntityType.ZOMBIE.create(level);
        helper.assertTrue(boss != null, "the boss must be creatable");

        BossSlainEvent attributedKill = new BossSlainEvent(dimension, 3, boss, killer);
        helper.assertTrue(attributedKill.killingPlayer().filter(killer::equals).isPresent(),
            "an attributed kill must carry the killing player's id");
        BossSlainEvent legacyKill = new BossSlainEvent(dimension, 3, boss);
        helper.assertTrue(legacyKill.killingPlayer().isEmpty(),
            "the legacy constructor must carry the documented unknown");
        helper.assertTrue(new BossSlainEvent(dimension, 3, boss, null).killingPlayer().isEmpty(),
            "a null attribution must read as unknown, not throw");
        helper.assertTrue(attributedKill.layer() == 3 && attributedKill.boss() == boss,
            "attribution must not disturb the existing payload");

        LayerCompletedEvent attributedCompletion = new LayerCompletedEvent(dimension, 4, killer);
        helper.assertTrue(attributedCompletion.completingPlayer().filter(killer::equals).isPresent(),
            "an attributed completion must carry the completing player's id");
        helper.assertTrue(attributedCompletion.layer() == 4,
            "attribution must not disturb the layer payload, got " + attributedCompletion.layer());
        helper.assertTrue(new LayerCompletedEvent(dimension, 4).completingPlayer().isEmpty(),
            "the legacy constructor must carry the documented unknown");
        helper.assertTrue(new LayerCompletedEvent(dimension, 4, null).completingPlayer().isEmpty(),
            "a null attribution must read as unknown, not throw");

        NeoForge.EVENT_BUS.post(attributedKill);
        NeoForge.EVENT_BUS.post(legacyKill);
        NeoForge.EVENT_BUS.post(attributedCompletion);
        helper.succeed();
    }

    /**
     * The floor-miniboss event (api#83): exact layer and floor from the core's
     * tag, the boss valid at fire, ADR-0003 attribution from birth, and the
     * constructor's payload validation.
     */
    @GameTest(template = "empty")
    public static void miniboss_slain_carries_tag_and_attribution(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ResourceKey<Level> dimension = level.dimension();
        UUID killer = UUID.randomUUID();
        Mob boss = EntityType.ZOMBIE.create(level);
        helper.assertTrue(boss != null, "the miniboss must be creatable");

        MinibossSlainEvent attributed = new MinibossSlainEvent(dimension, 5, 12, boss, killer);
        helper.assertTrue(attributed.layer() == 5 && attributed.floor() == 12,
            "the event must carry the tagged layer and floor, got " + attributed.layer() + "/" + attributed.floor());
        helper.assertTrue(attributed.boss() == boss, "the event must carry the boss");
        helper.assertTrue(attributed.killingPlayer().filter(killer::equals).isPresent(),
            "an attributed miniboss kill must carry the killing player's id");
        helper.assertTrue(attributed.dimension().equals(dimension),
            "the event must carry its dimension like every DungeonEvent");

        helper.assertTrue(new MinibossSlainEvent(dimension, 0, 0, boss, null).killingPlayer().isEmpty(),
            "a null attribution must read as unknown, not throw");

        boolean rejectedNegativeLayer = false;
        try {
            new MinibossSlainEvent(dimension, -1, 0, boss, null);
        } catch (IllegalArgumentException expected) {
            rejectedNegativeLayer = true;
        }
        helper.assertTrue(rejectedNegativeLayer, "a negative layer must be rejected");
        boolean rejectedNegativeFloor = false;
        try {
            new MinibossSlainEvent(dimension, 0, -1, boss, null);
        } catch (IllegalArgumentException expected) {
            rejectedNegativeFloor = true;
        }
        helper.assertTrue(rejectedNegativeFloor, "a negative floor must be rejected");

        NeoForge.EVENT_BUS.post(attributed);
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

    /**
     * The bridge's decline path (issue api#68): without Tensura the ModList
     * gate closes before any foreign class loads, and the write declines -
     * this harness carries neither Tensura nor manascore, so a write that
     * touched them anyway would crash the run instead of returning false.
     */
    @GameTest(template = "empty")
    public static void tensura_bridge_declines_without_the_mod(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var chunk = level.getChunkAt(helper.absolutePos(BlockPos.ZERO));
        helper.assertTrue(!TensuraMagiculeBridge.isUsable(),
                "the harness has no Tensura - the gate must be closed");
        helper.assertTrue(!TensuraMagiculeBridge.writeChunk(level, chunk),
                "without Tensura the write must decline, not crash");
        helper.succeed();
    }

    /**
     * The defer mechanism (issue api#68, spike core#6's timing instruction):
     * defer must QUEUE - a task enqueued from inside a tick, wherever the
     * caller sits (worker thread, chunk-load event, nested load), runs at the
     * next tick boundary and never inline at the call site. That deferral is
     * what carries scheduleWrite past Tensura's storage initialization.
     */
    @GameTest(template = "empty")
    public static void chunk_write_defers_to_the_next_tick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        boolean[] ran = {false};
        TensuraMagiculeBridge.defer(server, () -> ran[0] = true);
        helper.assertTrue(!ran[0],
                "defer must queue, never run inline - the deferral past Tensura's init");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(ran[0], "the deferred task must run by the next tick");
            helper.succeed();
        });
    }

    /**
     * The write's sample position (issue api#68): M is read at the chunk
     * centre's WORLD_SURFACE height - the same representative the
     * dungeon-mouth elevation sources use (#218), where mobs actually spawn.
     * Coordinate-encoding providers make the position observable: x*2^26+z
     * pins the centre (exact in double for any world coordinate), then y pins
     * the surface. The harness registers no other provider or modifier, so
     * the sampled value is exactly what the test's provider answers.
     *
     * <p>The y assertion pins the <b>raw</b> {@code WORLD_SURFACE} heightmap
     * value - the surface block, the same representative #218's mouth sources
     * use - rather than {@code Level.getHeight}, which reports one block
     * ABOVE it (vanilla's spawn-height convention). This test was first
     * written against {@code Level.getHeight} while the bridge samples the
     * heightmap the way #218 does, and that mismatch is exactly what the
     * block-state assertions caught. They pin the semantics independently of
     * either API call.</p>
     */
    @GameTest(template = "empty")
    public static void magicule_at_samples_the_surface_centre(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var chunk = level.getChunkAt(helper.absolutePos(BlockPos.ZERO));
        int centerX = chunk.getPos().getMinBlockX() + 8;
        int centerZ = chunk.getPos().getMinBlockZ() + 8;

        MagiculeProvider coordinates = (lvl, pos) ->
                OptionalDouble.of(pos.getX() * 67108864.0 + pos.getZ());
        MagiculeProvider surface = (lvl, pos) -> OptionalDouble.of(pos.getY());
        AmbientMagicule.registerProvider(coordinates);
        AmbientMagicule.registerProvider(surface);
        try {
            double sampled = TensuraMagiculeBridge.magiculeAt(level, chunk);
            double expectedXZ = centerX * 67108864.0 + centerZ;
            // Double.compare, not == : the pin is EXACT (both sides are
            // integer-valued doubles), which SpotBugs' floating-point-equality
            // rule cannot tell apart from a rounding bug.
            helper.assertTrue(Double.compare(sampled, expectedXZ) == 0,
                    "the sample must sit at the chunk centre: got " + sampled
                            + ", want " + expectedXZ);
            // Providers answer in registration order: drop coordinates so
            // surface answers. y must be the RAW WORLD_SURFACE value - NOT
            // Level.getHeight, which reads one above (see the javadoc).
            AmbientMagicule.removeProvider(coordinates);
            double sampledY = TensuraMagiculeBridge.magiculeAt(level, chunk);
            BlockPos onSurface = new BlockPos(centerX, (int) sampledY, centerZ);
            helper.assertTrue(!level.getBlockState(onSurface).isAir(),
                    "the sample must sit at the surface block: got y " + sampledY);
            helper.assertTrue(level.getBlockState(onSurface.above()).isAir(),
                    "the block above the surface sample must be air: got y " + sampledY);
        } finally {
            AmbientMagicule.removeProvider(coordinates);
            AmbientMagicule.removeProvider(surface);
        }
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

    /**
     * The deterministic stream contract (issue api#84): identical inputs draw
     * identical values, decision names are independent, and a later decision
     * cannot shift an earlier one's roll. Lives here rather than in JUnit for
     * the same reason as the codec round-trip: Minecraft's RandomSource stays
     * off the unit-test classpath, so only a live server can execute the
     * chain (the JUnit side pins the name validation, which needs no MC).
     */
    @GameTest(template = "empty")
    public static void determinism_streams_are_stable_and_independent(GameTestHelper helper) {
        long seed = 1234L;
        int x = 600;
        int y = -64;
        int z = 900;

        long first = Determinism.nextLong("infinite_dungeon:probe", seed, x, y, z);
        long repeat = Determinism.nextLong("infinite_dungeon:probe", seed, x, y, z);
        helper.assertTrue(first == repeat,
                "the same (decision, seed, position) must draw the same value: "
                        + first + " vs " + repeat);

        helper.assertTrue(Determinism.nextLong("infinite_dungeon:left", seed, x, y, z)
                        != Determinism.nextLong("infinite_dungeon:right", seed, x, y, z),
                "two decision names at one position must draw independent values");

        helper.assertTrue(Determinism.nextLong("infinite_dungeon:probe", seed, x, y, z)
                        != Determinism.nextLong("infinite_dungeon:probe", seed + 1, x, y, z),
                "the world seed must change a decision's value");
        helper.assertTrue(Determinism.nextLong("infinite_dungeon:probe", seed, x, y, z)
                        != Determinism.nextLong("infinite_dungeon:probe", seed, x, y + 1, z),
                "the position must change a decision's value");

        long before = Determinism.nextLong("infinite_dungeon:earlier", seed, x, y, z);
        Determinism.nextLong("infinite_dungeon:later", seed, x, y, z);
        long after = Determinism.nextLong("infinite_dungeon:earlier", seed, x, y, z);
        helper.assertTrue(before == after,
                "a new decision's stream must not shift an existing one: "
                        + before + " vs " + after);
        helper.succeed();
    }
}
