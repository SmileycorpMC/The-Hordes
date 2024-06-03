package net.smileycorp.hordes.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.client.render.RenderZombiePlayer;
import net.smileycorp.hordes.common.CommonProxy;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

@EventBusSubscriber(value = Side.CLIENT, modid = Constants.MODID)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		//Mob Infection
		if (InfectionConfig.enableMobInfection) MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		CapabilityManager.INSTANCE.register(HordeEventClient.class, new HordeEventClient.Storage(), HordeEventClient.Impl::new);
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityZombiePlayer.class, RenderZombiePlayer::new);
	}
}
