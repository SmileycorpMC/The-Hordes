package net.smileycorp.hordes.common.entities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.smileycorp.hordes.common.Constants;

public class HordesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MODID);

    public static final TagKey<Biome> HUSK_PLAYER_SPAWN_BIOMES = TagKey.create(Registries.BIOME, Constants.loc("husk_player_spawn_biomes"));

    public static final DeferredHolder<EntityType, EntityType<ZombiePlayer>> ZOMBIE_PLAYER = zombiePlayer("zombie_player", ZombiePlayer::new);
    public static final DeferredHolder<EntityType, EntityType<HuskPlayer>> HUSK_PLAYER = zombiePlayer("husk_player", HuskPlayer::new);
    public static final DeferredHolder<EntityType, EntityType<DrownedPlayer>> DROWNED_PLAYER = zombiePlayer("drowned_player", DrownedPlayer::new);

    private static <T extends Entity & PlayerZombie> DeferredHolder<EntityType<?>, EntityType<T>> zombiePlayer(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(name));
    }

}
