package net.smileycorp.hordes.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.smileycorp.hordes.common.entities.DrownedPlayer;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.entities.ZombiePlayer;

public class HordesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MODID);

    public static final RegistryObject<EntityType<ZombiePlayer>> ZOMBIE_PLAYER = zombiePlayer("zombie_player", ZombiePlayer::new);
    public static final RegistryObject<EntityType<DrownedPlayer>> DROWNED_PLAYER = zombiePlayer("drowned_player", DrownedPlayer::new);

    private static <T extends Entity & IZombiePlayer> RegistryObject<EntityType<T>> zombiePlayer(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(name));
    }

}
