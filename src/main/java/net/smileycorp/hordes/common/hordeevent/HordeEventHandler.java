package net.smileycorp.hordes.common.hordeevent;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
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
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.capability.HordeLevelData;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@EventBusSubscriber(modid=ModDefinitions.MODID)
public class HordeEventHandler {


	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerLevel level = server.overworld();
			if ((level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !CommonConfigHandler.pauseEventServer.get())) {
				int day = (int) Math.floor(level.getGameTime()/CommonConfigHandler.dayLength.get());
				int time = Math.round(level.getGameTime()%CommonConfigHandler.dayLength.get());
				HordeLevelData data = HordeLevelData.getData(level);
				if (((time >= CommonConfigHandler.hordeStartTime.get() && day == data.getNextDay()) || day > data.getNextDay())) {
					data.setNextDay(level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1) + CommonConfigHandler.hordeSpawnDays.get() + data.getNextDay());
				}
				data.setDirty();
			}
		}
	}

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		Player player = event.player;
		if (event.phase == Phase.END && player != null && !(player instanceof FakePlayer)) {
			Level level = player.level;
			if (!level.isClientSide && (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !CommonConfigHandler.pauseEventServer.get())) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IOngoingHordeEvent horde = optional.resolve().get();
					int day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
					int time = Math.round(level.getDayTime() % CommonConfigHandler.dayLength.get());
					if (horde != null && !horde.isActive(player)) {
						if (time >= CommonConfigHandler.hordeStartTime.get() && day >= horde.getNextDay() && (day!=0 || CommonConfigHandler.spawnFirstDay.get())) {
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

	@SubscribeEvent
	public void tryDespawn(LivingSpawnEvent.AllowDespawn event) {
		LivingEntity entity = event.getEntityLiving();
		LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
		if (optional.isPresent()) {
			IHordeSpawn cap = optional.resolve().get();
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
					LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
					if (optionalp.isPresent()) {
						if (optionalp.resolve().get().isActive(player)) {
							event.setResult(Result.DENY);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void update(LivingUpdateEvent event) {
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
							entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(entity, Player.class, true));
							LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
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

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Mob && !(entity instanceof Player)) {
			event.addCapability(ModDefinitions.getResource("HordeSpawn"), new IHordeSpawn.Provider());
		}
		if (entity instanceof Player && !(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("HordeEvent"), new IOngoingHordeEvent.Provider());
		}
	}

	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		Player player = event.getPlayer();
		Level level = player.level;
		if (!CommonConfigHandler.canSleepDuringHorde.get()) {
			if (!level.isClientSide) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IOngoingHordeEvent horde = optional.resolve().get();
					if ((horde.isHordeDay(player) && level.dimensionType().bedWorks() &! level.isDay()) || horde.isActive(player)) {
						event.setResult(BedSleepingProblem.OTHER_PROBLEM);
						player.displayClientMessage(new TranslatableComponent(ModDefinitions.hordeTrySleep), true);
					}
				}
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void playerClone(PlayerEvent.Clone event) {
		Player player = event.getPlayer();
		Player original = event.getOriginal();
		if (player != null && original != null &!(player instanceof FakePlayer || original instanceof FakePlayer)) {
			LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
			LazyOptional<IOngoingHordeEvent> optionalo = original.getCapability(Hordes.HORDE_EVENT, null);
			if (optionalp.isPresent() && optionalo.isPresent()) {
				IOngoingHordeEvent horde = optionalp.resolve().get();
				horde.readFromNBT(optionalo.resolve().get().writeToNBT(new CompoundTag()));
				horde.setPlayer(player);
			}
		}
	}
}
