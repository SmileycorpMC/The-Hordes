package net.smileycorp.hordes.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.data.ConfigFilesGenerator;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

@Mod(value = Constants.MODID)
@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Hordes {

	public Hordes() {
		HordesLogger.clearLog();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigHandler.config);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.config);
		//generate data files
		if (ConfigFilesGenerator.shouldGenerateFiles()) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->ConfigFilesGenerator::generateAssets);
			ConfigFilesGenerator.generateData();
		} else {
			HordesLogger.logInfo("Config files are up to date, skipping data/asset generation");
		}
	}

	@SubscribeEvent
	public static void constructMod(FMLConstructModEvent event) {
		//Horde Event
		if (CommonConfigHandler.enableHordeEvent.get()) {
			HordeEventPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
		} else {
			MinecraftForge.EVENT_BUS.unregister(HordeEventHandler.class);
		}
		//Mob Infection
		if (CommonConfigHandler.enableMobInfection.get()) {
			InfectionPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
		} else {
			MinecraftForge.EVENT_BUS.unregister(InfectionEventHandler.class);
		}
		DataRegistry.init();
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
		HordesInfection.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		HordesEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@SubscribeEvent
	public static void loadClient(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
	}

}
