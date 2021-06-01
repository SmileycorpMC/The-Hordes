package net.smileycorp.hordes.infection;

import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.ModDefinitions;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class HordesInfection {
	
	public static Potion INFECTED = new PotionInfected();
	public static DamageSource INFECTION_DAMAGE = new DamageSourceInfection();
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(INFECTED);
    }
	
}
