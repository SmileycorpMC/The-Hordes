package net.smileycorp.hordes.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
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
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.data.DefaultDataGenerator;
import net.smileycorp.hordes.common.hordeevent.data.HordeDataRegistry;
import net.smileycorp.hordes.common.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionEventHandler;
import net.smileycorp.hordes.common.infection.InfectionRegister;
import net.smileycorp.hordes.common.infection.capability.IInfection;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Constants.MODID)
@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Hordes {

	private static Logger logger = LogManager.getLogger(Constants.NAME);

	public final static Capability<IHordeEvent> HORDE_EVENT = CapabilityManager.get(new CapabilityToken<IHordeEvent>(){});
	public final static Capability<IHordeSpawn> HORDESPAWN = CapabilityManager.get(new CapabilityToken<IHordeSpawn>(){});
	public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = CapabilityManager.get(new CapabilityToken<IZombifyPlayer>(){});
	public final static Capability<IInfection> INFECTION = CapabilityManager.get(new CapabilityToken<IInfection>(){});

	public Hordes() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfigHandler.config);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.config);
		//generate data files
		if (DefaultDataGenerator.tryGenerateDataFiles()) logInfo("Generated default config files.");
		else logInfo("Config files exist, skipping generation");
	}

	@SubscribeEvent
	public static void constructMod(FMLConstructModEvent event) {
		//Horde Event
		if (CommonConfigHandler.enableHordeEvent.get()) {
			HordeDataRegistry.init();
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
	public static void loadComplete(FMLLoadCompleteEvent event) {
		//Mob Infection
		if (CommonConfigHandler.enableMobInfection.get()) {
			InfectionRegister.readConfig();
		}
	}

	@SubscribeEvent
	public static void loadClient(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

}
