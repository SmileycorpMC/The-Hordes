package net.smileycorp.hordes.common;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.smileycorp.hordes.common.hordeevent.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModDefinitions.modid, name=ModDefinitions.name, version = ModDefinitions.version, dependencies = ModDefinitions.dependencies)
public class Hordes {

	private static Logger logger = LogManager.getLogger(ModDefinitions.name);

	public static final SoundEvent HORDE_SOUND = new SoundEvent(ModDefinitions.getResource("horde_spawn"));

	@Instance(ModDefinitions.modid)
	public static Hordes INSTANCE;

	@SidedProxy(clientSide = ModDefinitions.client, serverSide = ModDefinitions.server)
	public static CommonProxy proxy;

	@CapabilityInject(IOngoingHordeEvent.class)
	public final static Capability<IOngoingHordeEvent> HORDE_EVENT = null;

	@CapabilityInject(IHordeSpawn.class)
	public final static Capability<IHordeSpawn> HORDESPAWN = null;

	@CapabilityInject(IZombifyPlayer.class)
	public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.postInit(event);
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event){
		proxy.serverStart(event);
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

}
