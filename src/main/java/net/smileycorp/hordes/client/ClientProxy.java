package net.smileycorp.hordes.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.client.render.RenderZombiePlayer;
import net.smileycorp.hordes.common.CommonProxy;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.EntityHuskPlayer;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.config.data.infection.InfectionData;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;
import net.smileycorp.hordes.infection.client.InfectionClientHandler;
import net.smileycorp.hordes.integration.oe.EntityDrownedPlayer;

@EventBusSubscriber(value = Side.CLIENT, modid = Constants.MODID)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ClientConfigHandler.syncConfig(new Configuration(event.getModConfigurationDirectory().toPath().resolve("hordes-client.cfg").toFile()));
		MinecraftForge.EVENT_BUS.register(HordeClientHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(InfectionClientHandler.INSTANCE);
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
		RenderingRegistry.registerEntityRenderingHandler(EntityZombiePlayer.class, rm -> new RenderZombiePlayer<>(rm,
				ClientConfigHandler.getZombiePlayerColour(), Constants.loc("textures/entity/layer/zombie_player_outer_layer.png"), false, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityHuskPlayer.class, rm -> new RenderZombiePlayer<>(rm,
				ClientConfigHandler.getHuskPlayerColour(), Constants.loc("textures/entity/layer/husk_player_outer_layer.png"), false, true));
		if (Loader.isModLoaded("oe")) RenderingRegistry.registerEntityRenderingHandler(EntityDrownedPlayer.class, rm -> new RenderZombiePlayer<>(rm,
				ClientConfigHandler.getDrownedPlayerColour(), Constants.loc("textures/entity/layer/drowned_player_outer_layer.png"), true, false));
	}

}
