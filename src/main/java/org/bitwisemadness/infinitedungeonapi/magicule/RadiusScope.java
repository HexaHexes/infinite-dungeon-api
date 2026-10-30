package org.bitwisemadness.infinitedungeonapi.magicule;

/**
 * A spherical area scoping a {@link ConcentrationModifier} to a neighbourhood
 * (plan section 7: every elevation source carries an amount AND a radius -
 * structures, nether portals and tower entrances raise the magicule of their
 * surroundings, not the whole world).
 *
 * <p>Pure by construction: primitives only, no Minecraft types, so the unit
 * suites load it without a Minecraft runtime - the same rule the value type
 * below is kept under. The boundary counts as inside (a source reaches its
 * radius exactly).</p>
 *
 * @param x the sphere centre's world X
 * @param y the sphere centre's world Y
 * @param z the sphere centre's world Z
 * @param radius the radius in blocks; must be non-negative, zero being a
 *               single block. {@link #GLOBAL} uses an infinite radius so every
 *               position applies - the degenerate positionless contract.
 */
public record RadiusScope(int x, int y, int z, double radius) {

    /** The everywhere-scope: every position applies (the positionless form). */
    public static final RadiusScope GLOBAL = new RadiusScope(0, 0, 0, Double.POSITIVE_INFINITY);

    public RadiusScope {
        if (radius < 0.0) {
            throw new IllegalArgumentException("radius must be non-negative, got " + radius);
        }
    }

    /** Convenience factory for a spherical scope. */
    public static RadiusScope around(int x, int y, int z, double radius) {
        return new RadiusScope(x, y, z, radius);
    }

    /** Whether the position falls inside the sphere (boundary included). */
    public boolean contains(int px, int py, int pz) {
        double dx = (double) px - x;
        double dy = (double) py - y;
        double dz = (double) pz - z;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
