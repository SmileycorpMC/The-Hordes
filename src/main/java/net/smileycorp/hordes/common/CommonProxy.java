package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.DataGenerator;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.hordeevent.HordeScriptLoader;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;
import net.smileycorp.hordes.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.command.CommandDebugHordeEvent;
import net.smileycorp.hordes.hordeevent.command.CommandSpawnWave;
import net.smileycorp.hordes.hordeevent.command.CommandStartHordeEvent;
import net.smileycorp.hordes.hordeevent.command.CommandStopHordeEvent;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		HordesLogger.clearLog();
		CommonConfigHandler.syncConfig(new Configuration(event.getSuggestedConfigurationFile()));
		DataGenerator.init(event);
		DataRegistry.init();
		MinecraftForge.EVENT_BUS.register(this);
		CapabilityManager.INSTANCE.register(ZombifyPlayer.class, new ZombifyPlayer.Storage(), ZombifyPlayer.Impl::new);
		CapabilityManager.INSTANCE.register(HordeSpawn.class, new HordeSpawn.Storage(), HordeSpawn.Impl::new);
		CapabilityManager.INSTANCE.register(Infection.class, new Infection.Storage(), Infection.Impl::new);
		//Horde Event
		if (HordeEventConfig.enableHordeEvent) {
			HordeEventPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
			HordeTableLoader.init(event);
			HordeScriptLoader.init(event);
		} else {
			MinecraftForge.EVENT_BUS.unregister(HordeEventHandler.class);
		}

		//Mob Infection
		if (InfectionConfig.enableMobInfection) {
			InfectionDataLoader.init(event);
			InfectionPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
		} else {
			MinecraftForge.EVENT_BUS.unregister(InfectionEventHandler.class);
		}
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
	}

	public void init(FMLInitializationEvent event) {}
	
	public void postInit(FMLPostInitializationEvent event) {
		//Horde Event
		if (HordeEventConfig.enableHordeEvent) {
			HordeTableLoader.INSTANCE.loadTables();
			HordeScriptLoader.INSTANCE.loadScripts();
		}
		//Mob Infection
		if (InfectionConfig.enableMobInfection) InfectionDataLoader.INSTANCE.loadInfectionData();
	}

	public void serverStart(FMLServerStartingEvent event) {
		if (HordeEventConfig.enableHordeEvent) {
			event.registerServerCommand(new CommandSpawnWave());
			event.registerServerCommand(new CommandStartHordeEvent());
			event.registerServerCommand(new CommandStopHordeEvent());
			event.registerServerCommand(new CommandDebugHordeEvent());
		}
	}

}
