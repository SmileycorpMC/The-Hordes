package net.smileycorp.hordes.common;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.commands.HordesCommand;
import net.smileycorp.hordes.common.commands.SubCommandListEntities;
import net.smileycorp.hordes.common.commands.SubCommandReload;
import net.smileycorp.hordes.common.commands.SubCommandSpawnZombie;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.ConfigDataManager;
import net.smileycorp.hordes.config.data.DataRegistry;
import net.smileycorp.hordes.config.data.hordeevent.HordeScriptLoader;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.config.data.infection.InfectionData;
import net.smileycorp.hordes.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.command.SubCommandSpawnWave;
import net.smileycorp.hordes.hordeevent.command.SubCommandStopHordeEvent;
import net.smileycorp.hordes.hordeevent.command.SubcommandDebugHordeEvent;
import net.smileycorp.hordes.hordeevent.command.SubcommandStartHordeEvent;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		HordesLogger.clearLog(false);
		HordesLogger.heading("LOADING CONFIGS");
		HordesLogger.blankLine();
		CommonConfigHandler.syncConfig(new Configuration(event.getSuggestedConfigurationFile()));
		DataRegistry.init();
		//generate data files
		ConfigDataManager.registerJsonLoader(new HordeTableLoader(event));
		ConfigDataManager.registerJsonLoader(new HordeScriptLoader(event));
		ConfigDataManager.registerJsonLoader(new InfectionData(event));
		HordesLogger.markVolatile();
		MinecraftForge.EVENT_BUS.register(this);
		CapabilityManager.INSTANCE.register(ZombifyPlayer.class, new ZombifyPlayer.Storage(), ZombifyPlayer.Impl::new);
		CapabilityManager.INSTANCE.register(HordeSpawn.class, new HordeSpawn.Storage(), HordeSpawn.Impl::new);
		CapabilityManager.INSTANCE.register(Infection.class, new Infection.Storage(), Infection.Impl::new);
		//Horde Event
		HordeEventPacketHandler.initPackets();
		InfectionPacketHandler.initPackets();
		if (HordeEventConfig.enableHordeEvent) MinecraftForge.EVENT_BUS.register(new HordeEventHandler());
		//Mob Infection
		if (InfectionConfig.enableMobInfection) MinecraftForge.EVENT_BUS.register(new InfectionEventHandler());
		MinecraftForge.EVENT_BUS.register(new MiscEventHandler());
	}

	public void init(FMLInitializationEvent event) {}
	
	public void postInit(FMLPostInitializationEvent event) {
		ConfigDataManager.reload();
	}

	public void serverStart(FMLServerStartingEvent event) {
		HordesCommand command = new HordesCommand();
		command.registerSubCommand("reload", new SubCommandReload());
		command.registerSubCommand("listEntities", new SubCommandListEntities());
		command.registerSubCommand("spawnZombie", new SubCommandSpawnZombie());
		if (HordeEventConfig.enableHordeEvent) {
			command.registerSubCommand("spawnWave", new SubCommandSpawnWave());
			command.registerSubCommand("start", new SubcommandStartHordeEvent());
			command.registerSubCommand("stop", new SubCommandStopHordeEvent());
			command.registerSubCommand("debug", new SubcommandDebugHordeEvent());
		}
		event.registerServerCommand(command);
	}

	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> registry = event.getRegistry();
		registry.register(Constants.INFECT_SOUND);
		registry.register(Constants.IMMUNE_SOUND);
	}

}
