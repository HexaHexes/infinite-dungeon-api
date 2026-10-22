package org.bitwisemadness.infinitedungeonapi.stats;

/**
 * The power stats a mob can be measured by.
 *
 * <p>The API treats every stat as an opaque provider-supplied value; the
 * meaning belongs to the provider. For the Tensura provider the mapping is
 * AP = aura, MP = magicule, and EP = the entity's stored energy (spike
 * finding).</p>
 */
public enum StatKind {

    /** Attack-oriented power. */
    AP,

    /** Magic-oriented power. */
    MP,

    /** Overall power: the default for boss ordering and spawn gating. */
    EP
}
