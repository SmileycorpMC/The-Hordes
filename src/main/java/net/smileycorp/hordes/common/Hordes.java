package net.smileycorp.hordes.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.common.data.DataGenerator;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.entities.HordesEntities;
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
		if (DataGenerator.shouldGenerateFiles()) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DataGenerator::generateAssets);
			DataGenerator.generateData();
		} else {
			HordesLogger.logInfo("Config files are up to date, skipping data/asset generation");
		}
	}

	@SubscribeEvent
	public static void constructMod(FMLConstructModEvent event) {
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
		HordesInfection.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		HordesEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		DataRegistry.init();
		//Horde Event
		if (HordeEventConfig.enableHordeEvent.get()) {
			HordeEventPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
		}
		//Mob Infection
		if (InfectionConfig.enableMobInfection.get()) {
			InfectionPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
		}
	}

	@SubscribeEvent
	public static void loadClient(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
	}

}
