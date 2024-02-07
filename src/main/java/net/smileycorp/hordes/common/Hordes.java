package net.smileycorp.hordes.common;

import net.minecraft.util.ResourceLocation;
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
import net.smileycorp.hordes.hordeevent.IHordeSpawn;
import net.smileycorp.hordes.hordeevent.IOngoingHordeEvent;
import net.smileycorp.hordes.infection.capability.IInfection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.modid, name=Constants.name, version = Constants.version, dependencies = Constants.dependencies)
public class Hordes {

	private static Logger logger = LogManager.getLogger(Constants.name);

	public static final ResourceLocation HORDE_SOUND = Constants.loc("horde_spawn");

	@Instance(Constants.modid)
	public static Hordes INSTANCE;

	@SidedProxy(clientSide = Constants.client, serverSide = Constants.server)
	public static CommonProxy proxy;

	@CapabilityInject(IOngoingHordeEvent.class)
	public final static Capability<IOngoingHordeEvent> HORDE_EVENT = null;

	@CapabilityInject(IHordeSpawn.class)
	public final static Capability<IHordeSpawn> HORDESPAWN = null;

	@CapabilityInject(IZombifyPlayer.class)
	public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = null;

	@CapabilityInject(IInfection.class)
	public final static Capability<IInfection> INFECTION = null;

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
