package net.smileycorp.hordes.infection;

import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.hordes.common.ModDefinitions;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class HordesInfection {
	
	public static Potion INFECTED = new PotionInfected();
	public static DamageSource INFECTION_DAMAGE = new DamageSourceInfection();
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(INFECTED);
    }
	
	@SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		IForgeRegistry<EntityEntry> registry = event.getRegistry();
		int ID = 201;
		/*EntityEntry ZOMBIE_PLAYER = EntityEntryBuilder.create().entity(EntityZombiePlayer.class)
				.id(ModDefinitions.getResource("zombie_player"), ID++)
				.name(ModDefinitions.getName("ZombiePlayer")).tracker(80, 3, true).build();
		registry.register(ZOMBIE_PLAYER);*/
    }
	
}
