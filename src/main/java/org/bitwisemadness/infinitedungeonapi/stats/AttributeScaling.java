package org.bitwisemadness.infinitedungeonapi.stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * The attribute scaling list (issue api#67): which attributes scale at spawn,
 * and by which curve of the plan's section 7 - DIFFICULTY (D) or MAGICULE (M).
 * The list answers the curve question only; the spawn-time application math
 * and the touched-attribute whitelist belong to the consumer (core issue #37).
 *
 * <p>The Tensura entries register themselves here, bundled per the placement
 * classification: on the first level load (every mod constructed by then,
 * so the attribute registry is complete) the namespace is enumerated - no
 * compile dependency on Tensura, no maintained id list, and attributes Tensura
 * adds later join by construction. The curve split is a name rule pinned
 * against the jar evidence (coordinate
 * {@code curse.maven:tensura-reincarnated-643695:8793360}: {@code max_magicule},
 * {@code magicule_gain}, {@code magicule_regeneration_multiplier},
 * {@code limited_spiritual_max_magicule} carry the magicule capacity - so
 * {@link #curveFor} keys {@code contains("magicule")} to MAGICULE while
 * everything else, including the {@code magic_*} defense family, keys
 * DIFFICULTY).</p>
 *
 * <p>The plan's {@code magicule.tensuraAttributeScaling} toggle (default ON)
 * gates the M-reading: while off, MAGICULE-keyed entries scale as DIFFICULTY
 * instead - still included, never dropped (core #37's "included by default"
 * clause survives the toggle; what the toggle removes is the magicule
 * reading). State is static and server-thread like every registry here;
 * {@link #reset} mirrors {@code AmbientMagicule.reset}'s reload-and-tests
 * contract.</p>
 */
public final class AttributeScaling {

    /** Which section 7 curve an attribute is keyed to. */
    public enum Curve {
        /** Scales with D (difficulty). */
        DIFFICULTY,
        /** Scales with M (ambient magicule concentration). */
        MAGICULE
    }

    /** One list row: an attribute and its effective curve. */
    public record ScaledAttribute(ResourceLocation attribute, Curve curve) {
    }

    /** Registration order preserved so consumers read a stable list. */
    private static final Map<ResourceLocation, Curve> KEYED = new LinkedHashMap<>();

    private static final Set<ResourceLocation> DISABLED = new HashSet<>();

    /** The plan's magicule-reading toggle, default ON (plan section 7). */
    private static boolean magiculeKeyedScaling = true;

    private AttributeScaling() {
    }

    /** Keys an attribute to a curve; re-registering updates the curve and keeps enablement. */
    public static void register(ResourceLocation attribute, Curve curve) {
        KEYED.put(attribute, curve);
    }

    /** Enables or disables one row without forgetting its curve. */
    public static void setEnabled(ResourceLocation attribute, boolean enabled) {
        if (KEYED.containsKey(attribute)) {
            if (enabled) {
                DISABLED.remove(attribute);
            } else {
                DISABLED.add(attribute);
            }
        }
    }

    /**
     * The plan's {@code magicule.tensuraAttributeScaling} toggle target
     * (default on). When off, MAGICULE-keyed entries scale as DIFFICULTY -
     * included, but without the magicule reading.
     */
    public static void setMagiculeKeyedScaling(boolean enabled) {
        magiculeKeyedScaling = enabled;
    }

    /**
     * The effective list: enabled rows with their curves resolved through the
     * toggle - registration order preserved.
     */
    public static List<ScaledAttribute> entries() {
        List<ScaledAttribute> effective = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Curve> entry : KEYED.entrySet()) {
            if (DISABLED.contains(entry.getKey())) {
                continue;
            }
            Curve curve = entry.getValue();
            if (curve == Curve.MAGICULE && !magiculeKeyedScaling) {
                curve = Curve.DIFFICULTY;
            }
            effective.add(new ScaledAttribute(entry.getKey(), curve));
        }
        return List.copyOf(effective);
    }

    /**
     * The curve name rule: an attribute whose id path carries "magicule" is
     * magicule-specific and keys M; everything else keys D. Pure - pinned by
     * the gametest against the jar-verified names.
     */
    public static Curve curveFor(String attributePath) {
        return attributePath.contains("magicule") ? Curve.MAGICULE : Curve.DIFFICULTY;
    }

    /**
     * Enumerates Tensura's registered attributes into the list (ModList-gated,
     * namespace-filtered, idempotent - safe to call from every level load,
     * which is also the only time every mod is guaranteed constructed).
     */
    public static void registerTensuraAttributes() {
        if (!ModList.get().isLoaded("tensura")) {
            return;
        }
        for (ResourceLocation id : BuiltInRegistries.ATTRIBUTE.keySet()) {
            if ("tensura".equals(id.getNamespace())) {
                register(id, curveFor(id.getPath()));
            }
        }
    }

    /** First level load: the registry is complete once every mod is constructed. */
    public static void onLevelLoad(LevelEvent.Load event) {
        registerTensuraAttributes();
    }

    /** Clears every registration; used by reload and tests (the AmbientMagicule contract). */
    public static void reset() {
        KEYED.clear();
        DISABLED.clear();
        magiculeKeyedScaling = true;
    }
}
