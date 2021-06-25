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
import net.smileycorp.hordes.common.hordeevent.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.command.CommandHordeDebug;
import net.smileycorp.hordes.common.hordeevent.command.CommandSpawnWave;
import net.smileycorp.hordes.common.hordeevent.command.CommandStartHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandStopHordeEvent;
import net.smileycorp.hordes.infection.InfectionEventHandler;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
		ConfigHandler.syncConfig();
		MinecraftForge.EVENT_BUS.register(this);
		
		//Horde Event
		HordeEventPacketHandler.initPackets();
		MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
		CapabilityManager.INSTANCE.register(IHordeSpawn.class, new IHordeSpawn.Storage(), new IHordeSpawn.Factory());
		
		//Zombie Gibbing
		/*GibbingPacketHandler.initPackets();
		MinecraftForge.EVENT_BUS.register(new GibbingEventHandler());
		CapabilityManager.INSTANCE.register(IZombieGibbing.class, new IZombieGibbing.Storage(), new IZombieGibbing.Factory());*/
		
		//Mob Infection
		MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
	}
	
	public void init(FMLInitializationEvent event) {
		
	}
	
	public void postInit(FMLPostInitializationEvent event) {
		
	}

	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandSpawnWave());
		event.registerServerCommand(new CommandStartHordeEvent());
		event.registerServerCommand(new CommandStopHordeEvent());
		event.registerServerCommand(new CommandHordeDebug());
	}
}
