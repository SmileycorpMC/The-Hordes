package net.smileycorp.hordes.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.data.DataGenerator;
import net.smileycorp.hordes.common.data.DataRegistry;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.hordeevent.HordeEventHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.client.InfectionClientHandler;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import java.nio.file.Path;
import java.util.Optional;

@Mod(value = Constants.MODID)
public class Hordes {

	public Hordes(ModContainer container, IEventBus bus) {
		HordesLogger.clearLog();
		container.registerConfig(ModConfig.Type.COMMON, CommonConfigHandler.config);
		container.registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.config);
		HordesInfection.EFFECTS.register(bus);
		HordesEntities.ENTITIES.register(bus);
		//generate data files
		if (DataGenerator.shouldGenerateFiles()) {
			if (FMLEnvironment.dist == Dist.CLIENT) DataGenerator.generateAssets();
			DataGenerator.generateData();
		} else {
			HordesLogger.logInfo("Config files are up to date, skipping data/asset generation");
		}
		bus.register(this);
		bus.addListener(HordeEventPacketHandler::initPackets);
		bus.addListener(InfectionPacketHandler::initPackets);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			bus.register(new ClientHandler());
			bus.addListener(InfectionClientHandler.INSTANCE::registerOverlays);
		}
	}

	@SubscribeEvent
	public void constructMod(FMLConstructModEvent event) {
		NeoForge.EVENT_BUS.register(new MiscEventHandler());
	}

	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
		DataRegistry.init();
		//Horde Event
		if (HordeEventConfig.enableHordeEvent.get()) NeoForge.EVENT_BUS.register(new HordeEventHandler());
		//Mob Infection
		if (InfectionConfig.enableMobInfection.get()) NeoForge.EVENT_BUS.register(new InfectionEventHandler());
	}

	@SubscribeEvent
	public void loadClient(FMLClientSetupEvent event) {
		NeoForge.EVENT_BUS.addListener(ClientHandler::renderNameplate);
		NeoForge.EVENT_BUS.register(HordeClientHandler.INSTANCE);
		NeoForge.EVENT_BUS.register(InfectionClientHandler.INSTANCE);
	}
	
	//attach zombie player provider to players
	@SubscribeEvent
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(HordesCapabilities.ZOMBIFY_PLAYER, EntityType.PLAYER, (entity, ctx) -> new ZombifyPlayer.Impl(entity));
		for (EntityType type : BuiltInRegistries.ENTITY_TYPE) {
			if (Mob.class.isAssignableFrom(type.getBaseClass()))
				event.registerEntity(HordesCapabilities.HORDESPAWN, type, (entity, ctx) -> new HordeSpawn.Impl());
			if (LivingEntity.class.isAssignableFrom(type.getBaseClass()))
				event.registerEntity(HordesCapabilities.INFECTION, type, (entity, ctx) -> new Infection.Impl());
		}
	}
	
	//register attributes for zombie players
	@SubscribeEvent
	public void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HordesEntities.ZOMBIE_PLAYER.get(), Zombie.createAttributes().build());
		event.put(HordesEntities.DROWNED_PLAYER.get(), Drowned.createAttributes().build());
		event.put(HordesEntities.HUSK_PLAYER.get(), Husk.createAttributes().build());
	}
	
	@SubscribeEvent
	public void addPackFinders(AddPackFindersEvent event) {
		Path path = FMLPaths.CONFIGDIR.get().resolve("hordes");
		event.addRepositorySource(consumer -> consumer.accept(Pack.readMetaAndCreate(
				new PackLocationInfo(path.toString(), Component.literal("Hordes Config"), PackSource.BUILT_IN, Optional.empty()),
				new PathPackResources.PathResourcesSupplier(path), event.getPackType(), new PackSelectionConfig(true, Pack.Position.TOP, false))));
	}

}
