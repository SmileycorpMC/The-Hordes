package net.smileycorp.hordes.infection;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.Constants;

@EventBusSubscriber(modid=Constants.MODID)
public class HordesInfection {
	
	public static final Potion INFECTED = new PotionInfected();
	public static final Potion IMMUNITY = new PotionHordes("immunity",  false, 0x00923A89);

	public static final IAttribute INFECTIVITY = new RangedAttribute(null, Constants.name("infectivity"), 0, 0, 1);
	public static final IAttribute INFECTION_RESISTANCE = new RangedAttribute(null, Constants.name("infection_resistance"), 0, 0, 1);
	
	public static final DamageSource INFECTION_DAMAGE = new DamageSourceInfection();
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(INFECTED);
		event.getRegistry().register(IMMUNITY);
    }
	
}
