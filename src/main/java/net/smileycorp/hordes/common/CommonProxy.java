package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.smileycorp.hordes.common.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventPacketHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventRegister;
import net.smileycorp.hordes.common.hordeevent.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandDebugHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandSpawnWave;
import net.smileycorp.hordes.common.hordeevent.command.CommandStartHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandStopHordeEvent;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.InfectionPacketHandler;
import net.smileycorp.hordes.infection.InfectionRegister;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
		ConfigHandler.syncConfig();
		MinecraftForge.EVENT_BUS.register(this);
		CapabilityManager.INSTANCE.register(IZombifyPlayer.class, new IZombifyPlayer.Storage(), () -> new IZombifyPlayer.Implementation());
		CapabilityManager.INSTANCE.register(IHordeSpawn.class, new IHordeSpawn.Storage(), () -> new IHordeSpawn.Implementation());
		CapabilityManager.INSTANCE.register(IOngoingHordeEvent.class, new IOngoingHordeEvent.Storage(), () -> new OngoingHordeEvent());
		//Horde Event
		if (ConfigHandler.enableHordeEvent) {
			HordeEventPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
		} else {
			MinecraftForge.EVENT_BUS.unregister(HordeEventHandler.class);
		}
		
		//Mob Infection
		if (ConfigHandler.enableMobInfection) {
			InfectionPacketHandler.initPackets();
			MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
		} else {
			MinecraftForge.EVENT_BUS.unregister(InfectionEventHandler.class);
		}
		
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
	}
	
	public void init(FMLInitializationEvent event) {
		
	}
	
	public void postInit(FMLPostInitializationEvent event) {
		//Horde Event
		if (ConfigHandler.enableHordeEvent) {
			HordeEventRegister.readConfig();
		}
		
		//Mob Infection
		if (ConfigHandler.enableMobInfection) {
			InfectionRegister.readConfig();
		}
	}

	public void serverStart(FMLServerStartingEvent event) {
		if (ConfigHandler.enableHordeEvent) {
			event.registerServerCommand(new CommandSpawnWave());
			event.registerServerCommand(new CommandStartHordeEvent());
			event.registerServerCommand(new CommandStopHordeEvent());
			event.registerServerCommand(new CommandDebugHordeEvent());
		}
	}
	
}
