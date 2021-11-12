package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.common.hordeevent.HordeEventRegister;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn.HordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionEventHandler;
import net.smileycorp.hordes.common.infection.InfectionRegister;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
		HordesInfection.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		HordesInfection.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(IZombifyPlayer.class, new IZombifyPlayer.Storage(), () -> new ZombifyPlayer());
		CapabilityManager.INSTANCE.register(IHordeSpawn.class, new IHordeSpawn.Storage(), () -> new HordeSpawn());
		CapabilityManager.INSTANCE.register(IOngoingHordeEvent.class, new IOngoingHordeEvent.Storage(), () -> new OngoingHordeEvent());
	}

	@SubscribeEvent
	public static void loadComplete(FMLLoadCompleteEvent event) {
		//Horde Event
		if (CommonConfigHandler.enableHordeEvent.get()) {
			HordeEventRegister.readConfig();
		}
		//Mob Infection
		if (CommonConfigHandler.enableMobInfection.get()) {
			InfectionRegister.readConfig();
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
