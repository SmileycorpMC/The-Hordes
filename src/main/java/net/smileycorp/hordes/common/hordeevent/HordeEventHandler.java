package net.smileycorp.hordes.common.hordeevent;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider.WorldSleepResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.smileycorp.atlas.api.entity.ai.EntityAIFindNearestTargetPredicate;
import net.smileycorp.atlas.api.entity.ai.EntityAIGoToEntityPos;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

import com.google.common.base.Predicate;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class HordeEventHandler {


	@SubscribeEvent
	public void worldTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			World world = server.getWorld(0);
			if ((world.getGameRules().getBoolean("doDaylightCycle") |! ConfigHandler.pauseEventServer)) {
				int day = (int) Math.floor(world.getWorldTime()/ConfigHandler.dayLength);
				int time = Math.round(world.getWorldTime()%ConfigHandler.dayLength);
				WorldDataHordeEvent data = WorldDataHordeEvent.getData(world);
				if (((time >= ConfigHandler.hordeStartTime && day == data.getNextDay()) || day > data.getNextDay())) {
					data.setNextDay(world.rand.nextInt(ConfigHandler.hordeSpawnVariation + 1) + ConfigHandler.hordeSpawnDays + data.getNextDay());
				}
				data.save();
			}
		}
	}

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		EntityPlayer player = event.player;
		if (event.phase == Phase.END && player != null && !(player instanceof FakePlayer)) {
			World world = player.world;
			if (!world.isRemote && (world.getGameRules().getBoolean("doDaylightCycle") |! ConfigHandler.pauseEventServer)) {
				if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
					IOngoingHordeEvent horde = player.getCapability(Hordes.HORDE_EVENT, null);
					int day = (int) Math.floor(world.getWorldTime() / ConfigHandler.dayLength);
					int time = Math.round(world.getWorldTime() % ConfigHandler.dayLength);
					if (horde != null && !horde.isActive(world)) {
						if (time >= ConfigHandler.hordeStartTime && day >= horde.getNextDay() && (day!=0 || ConfigHandler.spawnFirstDay)) {
							if (!horde.isActive(world)) {
								horde.tryStartEvent(ConfigHandler.hordeSpawnDuration, false);
							}
						}
					}
					if (horde.isActive(world)) {
						horde.update(world);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void tryDespawn(LivingSpawnEvent.AllowDespawn event) {
		World world = event.getWorld();
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.hasCapability(Hordes.HORDESPAWN, null)) {
			IHordeSpawn cap = entity.getCapability(Hordes.HORDESPAWN, null);
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
					if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
						if (player.getCapability(Hordes.HORDE_EVENT, null).isActive(world)) {
							event.setResult(Result.DENY);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onJoin(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		if (!world.isRemote && event.getEntity() instanceof EntityLiving) {
			EntityLiving entity = (EntityLiving) event.getEntity();
			if (entity.hasCapability(Hordes.HORDESPAWN, null)) {
				IHordeSpawn cap = entity.getCapability(Hordes.HORDESPAWN, null);
				if (cap.isHordeSpawned() && DataUtils.isValidUUID(cap.getPlayerUUID())) {
					entity.targetTasks.taskEntries.clear();
					if (entity instanceof EntityCreature) {
						entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, true));
						entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>((EntityCreature) entity, EntityPlayer.class, false));
					} else {
						entity.targetTasks.addTask(1, new EntityAIFindNearestTargetPredicate(entity, new Predicate<EntityLivingBase>(){
							@Override
							public boolean apply(EntityLivingBase entity) {
								return entity instanceof EntityPlayer;
							}}));
					}
					String uuid = cap.getPlayerUUID();
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
					if (player!=null) {
						if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
							IOngoingHordeEvent horde = player.getCapability(Hordes.HORDE_EVENT, null);
							entity.tasks.addTask(6, new EntityAIGoToEntityPos(entity, player));
							horde.registerEntity(entity);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (ConfigHandler.pauseEventServer) {
			for (World sworld : server.worlds) {
				if (sworld.getGameRules().getBoolean("doDaylightCycle") == false) {
					sworld.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
				}
			}
		}
	}

	@SubscribeEvent
	public void playerLeave(PlayerLoggedOutEvent event) {
		if (ConfigHandler.pauseEventServer) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server.getPlayerList().getPlayers().isEmpty()) {
				for (World sworld : server.worlds) {
					sworld.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
				}
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!entity.hasCapability(Hordes.HORDESPAWN, null) && entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
			event.addCapability(ModDefinitions.getResource("HordeSpawn"), new IHordeSpawn.Provider());
		}
		if (!entity.hasCapability(Hordes.HORDE_EVENT, null) && entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("HordeEvent"), new IOngoingHordeEvent.Provider((EntityPlayer) entity));
		}
	}

	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		if (!ConfigHandler.canSleepDuringHorde) {
			if (!world.isRemote) {
				if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
					IOngoingHordeEvent horde = player.getCapability(Hordes.HORDE_EVENT, null);
					if ((horde.isHordeDay(world) && world.provider.canSleepAt(player, event.getPos()) == WorldSleepResult.ALLOW &! world.isDaytime())
							|| horde.isActive(world)) {
						event.setResult(SleepResult.OTHER_PROBLEM);
						player.sendMessage(new TextComponentTranslation(ModDefinitions.hordeTrySleep));
					}
				}
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void playerClone(PlayerEvent.Clone event) {
		EntityPlayer player = event.getEntityPlayer();
		EntityPlayer original = event.getOriginal();
		if (player != null && original != null &!(player instanceof FakePlayer || original instanceof FakePlayer)) {
			if (player.hasCapability(Hordes.HORDE_EVENT, null) && original.hasCapability(Hordes.HORDE_EVENT, null)) {
				IOngoingHordeEvent horde = player.getCapability(Hordes.HORDE_EVENT, null);
				horde.readFromNBT(original.getCapability(Hordes.HORDE_EVENT, null).writeToNBT(new NBTTagCompound()));
				horde.setPlayer(player);
			}
		}
	}
}
