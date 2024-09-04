package net.smileycorp.hordes.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.Constants;

public class HordesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Constants.MODID);

    public static final RegistryObject<EntityType<ZombiePlayerEntity>> ZOMBIE_PLAYER = zombiePlayer("zombie_player", ZombiePlayerEntity::new);
    public static final RegistryObject<EntityType<HuskPlayerEntity>> HUSK_PLAYER = zombiePlayer("husk_player", HuskPlayerEntity::new);
    public static final RegistryObject<EntityType<DrownedPlayerEntity>> DROWNED_PLAYER = zombiePlayer("drowned_player", DrownedPlayerEntity::new);

    private static <T extends Entity & PlayerZombie> RegistryObject<EntityType<T>> zombiePlayer(String name, EntityType.IFactory<T> factory) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, EntityClassification.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(name));
    }

}
