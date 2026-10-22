package org.bitwisemadness.infinitedungeonapi.spawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;

/**
 * Game-facing entry point for the classification chain and the gated-spawn
 * test. The rules themselves live in {@link ClassificationRules} and
 * {@link NaturalSpawns}, which stay Minecraft-free and unit-tested.
 */
public final class MobClassification {

    private MobClassification() {
    }

    /** The derived or overridden category of an entity. */
    public static SpawnCategory categoryOf(Entity entity) {
        if (entity == null) {
            return SpawnCategory.PASSIVE;
        }
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        ClassificationFacts facts = new ClassificationFacts(
                entity instanceof NeutralMob,
                entity instanceof Monster,
                entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER);
        return ClassificationRules.resolve(
                id == null ? java.util.Optional.empty() : ClassificationOverrides.get(id.toString()),
                facts);
    }

    /** Whether a spawn of this type is in scope for magicule gating. */
    public static boolean isGatedSpawn(MobSpawnType spawnType) {
        return spawnType != null && NaturalSpawns.allows(spawnType.name());
    }
}
