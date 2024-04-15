package net.smileycorp.hordes.common.entities;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.smileycorp.hordes.common.Constants;

public class HordesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Constants.MODID);

    public static final TagKey<Biome> HUSK_PLAYER_SPAWN_BIOMES = TagKey.m_203882_(Registry.BIOME_REGISTRY, Constants.loc("husk_player_spawn_biomes"));

    public static final RegistryObject<EntityType<ZombiePlayerEntity>> ZOMBIE_PLAYER = zombiePlayer("zombie_player", ZombiePlayerEntity::new);
    public static final RegistryObject<EntityType<HuskPlayerEntity>> HUSK_PLAYER = zombiePlayer("husk_player", HuskPlayerEntity::new);
    public static final RegistryObject<EntityType<DrownedPlayerEntity>> DROWNED_PLAYER = zombiePlayer("drowned_player", DrownedPlayerEntity::new);

    private static <T extends Entity & PlayerZombie> RegistryObject<EntityType<T>> zombiePlayer(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(name));
    }

}
