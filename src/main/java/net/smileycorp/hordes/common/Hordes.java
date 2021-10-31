package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventPacketHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventRegister;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeWorldData;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandDebugHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandSpawnWave;
import net.smileycorp.hordes.common.hordeevent.command.CommandStartHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandStopHordeEvent;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.InfectionPacketHandler;
import net.smileycorp.hordes.infection.InfectionRegister;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.modid)
public class Hordes {

	private static Logger logger = LogManager.getLogger(ModDefinitions.name);

	@CapabilityInject(IOngoingHordeEvent.class)
	public final static Capability<IOngoingHordeEvent> HORDE_EVENT = null;

	@CapabilityInject(IHordeWorldData.class)
	public final static Capability<IOngoingHordeEvent> HORDE_WORLD_DATA = null;


	@CapabilityInject(IHordeSpawn.class)
	public final static Capability<IHordeSpawn> HORDESPAWN = null;

	@CapabilityInject(IZombifyPlayer.class)
	public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = null;

	public Hordes() {

	}

	public void preInit(FMLConstructModEvent event) {
		ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
		ConfigHandler.syncConfig();
		MinecraftForge.EVENT_BUS.register(this);
		CapabilityManager.INSTANCE.register(IZombifyPlayer.class, new IZombifyPlayer.Storage(), () -> new ZombifyPlayer());
		CapabilityManager.INSTANCE.register(IHordeSpawn.class, new IHordeSpawn.Storage(), () -> new IHordeSpawn.HordeSpawn());
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

	public void postInit(FMLLoadCompleteEvent event) {
		//Horde Event
		if (ConfigHandler.enableHordeEvent) {
			HordeEventRegister.readConfig();
		}

		//Mob Infection
		if (ConfigHandler.enableMobInfection) {
			InfectionRegister.readConfig();
		}
	}

	public void serverStart(RegisterCommandsEvent event) {
		if (ConfigHandler.enableHordeEvent) {
			event.registerServerCommand(new CommandSpawnWave());
			event.registerServerCommand(new CommandStartHordeEvent());
			event.registerServerCommand(new CommandStopHordeEvent());
			event.registerServerCommand(new CommandDebugHordeEvent());
		}
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

}
