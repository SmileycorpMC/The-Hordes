package net.smileycorp.hordes.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.smileycorp.hordes.common.entities.DrownedPlayer;
import net.smileycorp.hordes.common.entities.ZombiePlayer;

public class HordesEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MODID);
    public static final RegistryObject<EntityType<DrownedPlayer>> DROWNED_PLAYER = ENTITIES.register("drowned_player", () ->
            EntityType.Builder.<DrownedPlayer>of(DrownedPlayer::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build("drowned_player"));
    public static final RegistryObject<EntityType<ZombiePlayer>> ZOMBIE_PLAYER = ENTITIES.register("zombie_player", () ->
            EntityType.Builder.<ZombiePlayer>of(ZombiePlayer::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build("zombie_player"));
}
