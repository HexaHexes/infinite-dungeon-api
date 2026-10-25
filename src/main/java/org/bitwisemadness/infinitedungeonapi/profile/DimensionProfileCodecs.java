package org.bitwisemadness.infinitedungeonapi.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * JSON codecs for dimension profiles.
 *
 * <p>Separate from the value types so those stay free of Mojang dependencies
 * and unit-testable without a game runtime, following the data layer's rule.
 * The codec lives on the game-facing side: the reload listener is its only
 * production caller, and the GameTest is its round-trip proof.</p>
 */
public final class DimensionProfileCodecs {

    private DimensionProfileCodecs() {
    }

    /** The {@code { min, max }} floor-count pair. */
    public static final Codec<FloorsPerLayer> FLOORS_PER_LAYER = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(FloorsPerLayer::min),
            Codec.INT.fieldOf("max").forGetter(FloorsPerLayer::max)
    ).apply(instance, FloorsPerLayer::new));

    /** The {@code { fodder, elite, boss }} pool ids. */
    public static final Codec<ProfilePools> POOLS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("fodder").forGetter(ProfilePools::fodder),
            Codec.STRING.optionalFieldOf("elite").forGetter(ProfilePools::elite),
            Codec.STRING.optionalFieldOf("boss").forGetter(ProfilePools::boss)
    ).apply(instance, ProfilePools::new));

    /** One curve's coefficients. */
    public static final Codec<CurveCoefficients> CURVE_COEFFICIENTS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("base").forGetter(CurveCoefficients::base),
            Codec.DOUBLE.fieldOf("linearPerFloor").forGetter(CurveCoefficients::linearPerFloor),
            Codec.DOUBLE.fieldOf("quadraticPerLayer").forGetter(CurveCoefficients::quadraticPerLayer)
    ).apply(instance, CurveCoefficients::new));

    /** The {@code { difficulty, magicule, density }} overrides. */
    public static final Codec<CurveOverrides> CURVES = RecordCodecBuilder.create(instance -> instance.group(
            CURVE_COEFFICIENTS.optionalFieldOf("difficulty").forGetter(CurveOverrides::difficulty),
            CURVE_COEFFICIENTS.optionalFieldOf("magicule").forGetter(CurveOverrides::magicule),
            CURVE_COEFFICIENTS.optionalFieldOf("density").forGetter(CurveOverrides::density)
    ).apply(instance, CurveOverrides::new));

    /** The {@code { respawnCooldown, powerBandPercent }} guardian rules. */
    public static final Codec<GuardianRules> GUARDIAN = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("respawnCooldown").forGetter(GuardianRules::respawnCooldown),
            Codec.DOUBLE.fieldOf("powerBandPercent").forGetter(GuardianRules::powerBandPercent)
    ).apply(instance, GuardianRules::new));

    /** {@code entry_portal} or {@code world_spawn}. */
    public static final Codec<PortalReturnMode> RETURN_MODE =
            Codec.STRING.xmap(PortalReturnMode::parse, PortalReturnMode::serializedName);

    /** The {@code { returnMode, allowConstruction, reformMissingPortal }} policy. */
    public static final Codec<PortalPolicy> PORTAL = RecordCodecBuilder.create(instance -> instance.group(
            RETURN_MODE.optionalFieldOf("returnMode", PortalReturnMode.ENTRY_PORTAL)
                    .forGetter(PortalPolicy::returnMode),
            Codec.BOOL.optionalFieldOf("allowConstruction", true)
                    .forGetter(PortalPolicy::allowConstruction),
            Codec.BOOL.optionalFieldOf("reformMissingPortal", true)
                    .forGetter(PortalPolicy::reformMissingPortal)
    ).apply(instance, PortalPolicy::new));

    /** The {@code { entry, layer }} announcement keys. */
    public static final Codec<ProfileAnnouncements> ANNOUNCEMENTS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("entry", "").forGetter(ProfileAnnouncements::entry),
            Codec.STRING.optionalFieldOf("layer", "").forGetter(ProfileAnnouncements::layer)
    ).apply(instance, ProfileAnnouncements::new));

    /** The whole profile document. */
    public static final MapCodec<DimensionProfile> PROFILE = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(DimensionProfile::dimension),
            Codec.INT.optionalFieldOf("layerLimit").forGetter(DimensionProfile::layerLimit),
            FLOORS_PER_LAYER.optionalFieldOf("floorsPerLayer").forGetter(DimensionProfile::floorsPerLayer),
            POOLS.optionalFieldOf("pools").forGetter(DimensionProfile::pools),
            CURVES.optionalFieldOf("curves").forGetter(DimensionProfile::curves),
            GUARDIAN.optionalFieldOf("guardian").forGetter(DimensionProfile::guardian),
            PORTAL.optionalFieldOf("portal").forGetter(DimensionProfile::portal),
            ANNOUNCEMENTS.optionalFieldOf("announcements").forGetter(DimensionProfile::announcements),
            Codec.BOOL.optionalFieldOf("spawnSafetyExempt").forGetter(DimensionProfile::spawnSafetyExempt)
    ).apply(instance, DimensionProfile::new));
}
