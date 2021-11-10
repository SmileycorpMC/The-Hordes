package net.smileycorp.hordes.common.infection;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.DrownedPlayerEntity;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;

@EventBusSubscriber(modid=ModDefinitions.MODID)
public class HordesInfection {

	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, ModDefinitions.MODID);
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ModDefinitions.MODID);

	public static RegistryObject<Effect> INFECTED = EFFECTS.register("infected", () -> new InfectedEffect());

	public static RegistryObject<EntityType<ZombiePlayerEntity>> ZOMBIE_PLAYER = ENTITIES.register("zombie_player", () ->
		EntityType.Builder.<ZombiePlayerEntity>of(ZombiePlayerEntity::new, EntityClassification.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build("zombie_player"));
	public static RegistryObject<EntityType<DrownedPlayerEntity>> DROWNED_PLAYER = ENTITIES.register("drowned_player", () ->
		EntityType.Builder.<DrownedPlayerEntity>of(DrownedPlayerEntity::new, EntityClassification.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build("drowned_player"));

	public static DamageSource INFECTION_DAMAGE = new DamageSourceInfection();

}
