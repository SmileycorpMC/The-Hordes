package net.smileycorp.hordes.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.CommonProxy;
import net.smileycorp.hordes.common.ModDefinitions;

@EventBusSubscriber(value = Side.CLIENT, modid = ModDefinitions.modid)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		//MinecraftForge.EVENT_BUS.register(new GibbingClientEventHandler());
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
		//RenderingRegistry.registerEntityRenderingHandler(EntityZombiePlayer.class, m-> new RenderZombiePlayer(m));
	}
}
