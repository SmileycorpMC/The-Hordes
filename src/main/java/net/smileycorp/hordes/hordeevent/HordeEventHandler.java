package net.smileycorp.hordes.hordeevent;

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
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.ai.EntityAIHordeTrackPlayer;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.capability.IOngoingHordeEvent;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordeEvent;

import java.util.UUID;

@EventBusSubscriber(modid=Constants.MODID)
public class HordeEventHandler {


	@SubscribeEvent
	public void worldTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			World world = server.getWorld(0);
			if ((world.getGameRules().getBoolean("doDaylightCycle") |! HordeEventConfig.pauseEventServer)) {
				int day = (int) Math.floor(world.getWorldTime()/ HordeEventConfig.dayLength);
				WorldDataHordeEvent data = WorldDataHordeEvent.getData(world);
				if ( day >= data.getNextDay()) {
					data.setNextDay(world.rand.nextInt(HordeEventConfig.hordeSpawnVariation + 1) + HordeEventConfig.hordeSpawnDays + data.getNextDay());
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
			if (!world.isRemote && (world.getGameRules().getBoolean("doDaylightCycle") |! HordeEventConfig.pauseEventServer)) {
				if (player.hasCapability(HordesCapabilities.HORDE_EVENT, null)) {
					IOngoingHordeEvent horde = player.getCapability(HordesCapabilities.HORDE_EVENT, null);
					int day = (int) Math.floor(world.getWorldTime() / HordeEventConfig.dayLength);
					int time = Math.round(world.getWorldTime() % HordeEventConfig.dayLength);
					if (horde != null && !horde.isActive(world)) {
						if (time >= HordeEventConfig.hordeStartTime && day >= horde.getNextDay() && (day!=0 || HordeEventConfig.spawnFirstDay)) {
							if (!horde.isActive(world)) {
								horde.tryStartEvent(HordeEventConfig.hordeSpawnDuration, false);
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
		if (entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) {
			HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
					if (player != null) {
						if (player.hasCapability(HordesCapabilities.HORDE_EVENT, null)) {
							if (player.getCapability(HordesCapabilities.HORDE_EVENT, null).isActive(world)) {
								event.setResult(Result.DENY);
							}
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
			if (entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) {
				HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
				if (cap.isHordeSpawned() && DataUtils.isValidUUID(cap.getPlayerUUID())) {
					entity.targetTasks.taskEntries.clear();
					if (entity instanceof EntityCreature) {
						entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, true));
						entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>((EntityCreature) entity, EntityPlayer.class, false));
					} else {
						entity.targetTasks.addTask(1, new EntityAIFindNearestTargetPredicate(entity, (e)-> e instanceof EntityPlayer));
					}
					String uuid = cap.getPlayerUUID();
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
					if (player != null) {
						if (player.hasCapability(HordesCapabilities.HORDE_EVENT, null)) {
							IOngoingHordeEvent horde = player.getCapability(HordesCapabilities.HORDE_EVENT, null);
							entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player));
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
		if (HordeEventConfig.pauseEventServer) {
			for (World sworld : server.worlds) {
				if (sworld.getGameRules().getBoolean("doDaylightCycle") == false) {
					sworld.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
				}
			}
		}
	}

	@SubscribeEvent
	public void playerLeave(PlayerLoggedOutEvent event) {
		if (HordeEventConfig.pauseEventServer) {
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
		if (entity == null) return;
		if (entity.world == null) return;
		if (entity.world.isRemote) return;
		if (!entity.hasCapability(HordesCapabilities.HORDESPAWN, null) && entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
			event.addCapability(Constants.loc("HordeSpawn"), new HordeSpawn.Provider());
		}
		if (!entity.hasCapability(HordesCapabilities.HORDE_EVENT, null) && entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
			event.addCapability(Constants.loc("HordeEvent"), new IOngoingHordeEvent.Provider((EntityPlayer) entity));
		}
	}

	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		if (!HordeEventConfig.canSleepDuringHorde) {
			if (!world.isRemote) {
				if (player.hasCapability(HordesCapabilities.HORDE_EVENT, null)) {
					IOngoingHordeEvent horde = player.getCapability(HordesCapabilities.HORDE_EVENT, null);
					if ((horde.isHordeDay(world) && world.provider.canSleepAt(player, event.getPos()) == WorldSleepResult.ALLOW &! world.isDaytime())
							|| horde.isActive(world)) {
						event.setResult(SleepResult.OTHER_PROBLEM);
						player.sendMessage(new TextComponentTranslation(Constants.hordeTrySleep));
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
			if (player.hasCapability(HordesCapabilities.HORDE_EVENT, null) && original.hasCapability(HordesCapabilities.HORDE_EVENT, null)) {
				IOngoingHordeEvent horde = player.getCapability(HordesCapabilities.HORDE_EVENT, null);
				horde.readFromNBT(original.getCapability(HordesCapabilities.HORDE_EVENT, null).writeToNBT(new NBTTagCompound()));
				horde.setPlayer(player);
			}
		}
	}
}
