package net.smileycorp.hordes.common.entities;

import net.minecraft.core.registries.Registries;
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
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MODID);

    public static final TagKey<Biome> HUSK_PLAYER_SPAWN_BIOMES = TagKey.create(Registries.f_256952_, Constants.loc("husk_player_spawn_biomes"));

    public static final RegistryObject<EntityType<ZombiePlayer>> ZOMBIE_PLAYER = zombiePlayer("zombie_player", ZombiePlayer::new);
    public static final RegistryObject<EntityType<HuskPlayer>> HUSK_PLAYER = zombiePlayer("husk_player", HuskPlayer::new);
    public static final RegistryObject<EntityType<DrownedPlayer>> DROWNED_PLAYER = zombiePlayer("drowned_player", DrownedPlayer::new);

    private static <T extends Entity & PlayerZombie> RegistryObject<EntityType<T>> zombiePlayer(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(name));
    }

}
