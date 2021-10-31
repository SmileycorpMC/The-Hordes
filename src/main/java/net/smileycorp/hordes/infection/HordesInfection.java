package net.smileycorp.hordes.infection;

import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class HordesInfection {

	public static Effect INFECTED = new InfectedEffect();
	public static DamageSource INFECTION_DAMAGE = new DamageSourceInfection();

	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INFECTED);
    }

	@SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		IForgeRegistry<EntityEntry> registry = event.getRegistry();
		int ID = 201;
		EntityEntry ZOMBIE_PLAYER = EntityEntryBuilder.create().entity(ZombiePlayerEntity.class)
				.id(ModDefinitions.getResource("zombie_player"), ID++)
				.name(ModDefinitions.getName("ZombiePlayer")).tracker(80, 3, true).build();
		registry.register(ZOMBIE_PLAYER);
    }

}
