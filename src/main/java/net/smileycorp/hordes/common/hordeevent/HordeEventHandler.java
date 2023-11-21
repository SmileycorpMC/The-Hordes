package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.common.hordeevent.data.scripts.HordeScriptLoader;

import java.util.UUID;

@EventBusSubscriber(modid=Constants.MODID)
public class HordeEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Mob) {
			event.addCapability(Constants.loc("HordeSpawn"), new IHordeSpawn.Provider());
		}
		if (entity instanceof Player && !(entity instanceof FakePlayer)) {
			event.addCapability(Constants.loc("HordeEvent"), new IHordeEvent.Provider());
		}
	}

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(HordeTableLoader.INSTANCE);
		event.addListener(HordeScriptLoader.INSTANCE);
	}

	//update the next day in the horde level data
	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.START &! CommonConfigHandler.hordesCommandOnly.get()) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerLevel level = server.overworld();
			if ((level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) |! CommonConfigHandler.pauseEventServer.get())) {
				int day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
				HordeSavedData data = HordeSavedData.getData(level);
				if (day >= data.getNextDay()) {
					data.setNextDay(level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1) + CommonConfigHandler.hordeSpawnDays.get() + data.getNextDay());
				}
				data.save();
			}
		}
	}

	//spawn the horde at the correct time
	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		Player player = event.player;
		if (event.phase == Phase.END && player != null && !(player instanceof FakePlayer)) {
			Level level = player.level;
			if (!level.isClientSide && (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !CommonConfigHandler.pauseEventServer.get())) {
				LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IHordeEvent horde = optional.resolve().get();
					int day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
					int time = Math.round(level.getDayTime() % CommonConfigHandler.dayLength.get());
					if (horde != null && !horde.isActive(player)) {
						if (time >= CommonConfigHandler.hordeStartTime.get() && day >= horde.getNextDay() && (day>0 || CommonConfigHandler.spawnFirstDay.get())) {
							horde.tryStartEvent(player, CommonConfigHandler.hordeSpawnDuration.get(), false);
						}
					}
					if (horde.isActive(player)) {
						horde.update(player);
					}
				}
			}
		}
	}

	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(MobSpawnEvent.AllowDespawn event) {
		LivingEntity entity = event.getEntity();
		if (entity.level.isClientSide) return;
		LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
		if (optional.isPresent()) {
			IHordeSpawn cap = optional.resolve().get();
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
					if (player != null) {
						LazyOptional<IHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
						if (optionalp.isPresent()) {
							if (optionalp.resolve().get().isActive(player)) {
								event.setResult(Result.DENY);
							}
						}
					}
				}
			}
		}
	}

	//remove entities from horde when they die
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof Mob) {
			Mob entity = (Mob) event.getEntity();
			if (entity.level.isClientSide) return;
			LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
			if (optional.isPresent()) {
				IHordeSpawn cap = optional.resolve().get();
				if (cap.isHordeSpawned()) {
					String uuid = cap.getPlayerUUID();
					if (DataUtils.isValidUUID(uuid)) {
						Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
						if (player != null) {
							LazyOptional<IHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
							if (optionalp.isPresent()) {
								optionalp.resolve().get().removeEntity(entity);
							}
						}
					}
				}
			}
		}
	}

	//sync entity capabilities when added to level
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void update(LivingTickEvent event) {
		Level level = event.getEntity().level;
		if (!level.isClientSide && event.getEntity() instanceof Mob && level.dimension() == Level.OVERWORLD && event.getEntity().tickCount%5==0) {
			Mob entity = (Mob) event.getEntity();
			LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
			if (optional.isPresent()) {
				IHordeSpawn cap = optional.resolve().get();
				if (cap.isHordeSpawned() &! cap.isSynced()) {
					String uuid = cap.getPlayerUUID();
					if (DataUtils.isValidUUID(uuid)) {
						Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
						if (player!=null) {
							entity.targetSelector.getRunningGoals().forEach((goal) -> goal.stop());
							if (entity instanceof PathfinderMob) {
								entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
							}
							entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
							LazyOptional<IHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
							if (optionalp.isPresent()) {
								optionalp.resolve().get().registerEntity(entity);
								entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
							}
							cap.setSynced();
						}
					}
				}
			}
		}
	}

	//pause server day cycle if no players are logged on
	@SubscribeEvent
	public void playerLeave(PlayerLoggedOutEvent event) {
		if (CommonConfigHandler.pauseEventServer.get()) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server.getPlayerCount() == 0) {
				server.getAllLevels().forEach( (level) -> {
					if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == true) {
						level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
					}
				});
			}
		}
	}

	//resume server day cycle when a player joins
	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (CommonConfigHandler.pauseEventServer.get()) {
			server.getAllLevels().forEach( (level) -> {
				if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == false) {
					level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
				}
			});
		}
	}

	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		Player player = event.getEntity();
		Level level = player.level;
		if (!CommonConfigHandler.canSleepDuringHorde.get()) {
			if (!level.isClientSide) {
				LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IHordeEvent horde = optional.resolve().get();
					if ((horde.isHordeDay(player) && level.dimensionType().bedWorks() &! level.isDay()) || horde.isActive(player)) {
						event.setResult(BedSleepingProblem.OTHER_PROBLEM);
						player.displayClientMessage(MutableComponent.create(new TranslatableContents(Constants.hordeTrySleep, null, new Object[]{})), true);
					}
				}
			}
		}
	}

	//copy horde event capability to new player instance on death
	@SubscribeEvent(receiveCanceled = true)
	public void playerClone(PlayerEvent.Clone event) {
		Player player = event.getEntity();
		Player original = event.getOriginal();
		if (player != null && original != null &!(player instanceof FakePlayer || original instanceof FakePlayer)) {
			LazyOptional<IHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
			LazyOptional<IHordeEvent> optionalo = original.getCapability(Hordes.HORDE_EVENT, null);
			if (optionalp.isPresent() && optionalo.isPresent()) {
				IHordeEvent horde = optionalp.resolve().get();
				horde.readFromNBT(optionalo.resolve().get().writeToNBT(new CompoundTag()));
				horde.setPlayer(player);
			}
		}
	}

}
