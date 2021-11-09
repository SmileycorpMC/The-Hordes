package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.client.ClientInfectionEventHandler;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventRegister;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandDebugHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandSpawnWave;
import net.smileycorp.hordes.common.hordeevent.command.CommandStartHordeEvent;
import net.smileycorp.hordes.common.hordeevent.command.CommandStopHordeEvent;
import net.smileycorp.hordes.common.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.InfectionRegister;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
public class Hordes {

	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	@CapabilityInject(IOngoingHordeEvent.class)
	public final static Capability<IOngoingHordeEvent> HORDE_EVENT = null;

	@CapabilityInject(IHordeSpawn.class)
	public final static Capability<IHordeSpawn> HORDESPAWN = null;

	@CapabilityInject(IZombifyPlayer.class)
	public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = null;

	public Hordes() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigHandler.config);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.config);
	}


	public void constructMod(FMLConstructModEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		CapabilityManager.INSTANCE.register(IZombifyPlayer.class, new IZombifyPlayer.Storage(), () -> new ZombifyPlayer());
		CapabilityManager.INSTANCE.register(IHordeSpawn.class, new IHordeSpawn.Storage(), () -> new IHordeSpawn.HordeSpawn());
		CapabilityManager.INSTANCE.register(IOngoingHordeEvent.class, new IOngoingHordeEvent.Storage(), () -> new OngoingHordeEvent());
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
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
		HordesInfection.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		HordesInfection.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public void loadComplete(FMLLoadCompleteEvent event) {
		//Horde Event
		if (CommonConfigHandler.enableHordeEvent.get()) {
			HordeEventRegister.readConfig();
		}
		//Mob Infection
		if (CommonConfigHandler.enableMobInfection.get()) {
			InfectionRegister.readConfig();
		}
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
	}

	public void serverStart(RegisterCommandsEvent event) {
		if (CommonConfigHandler.enableHordeEvent.get()) {
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
