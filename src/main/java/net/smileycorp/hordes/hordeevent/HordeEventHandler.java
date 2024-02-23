package net.smileycorp.hordes.hordeevent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.ai.HordeTrackPlayerGoal;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

public class HordeEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		for (EntityType type : BuiltInRegistries.ENTITY_TYPE) {
			event.registerEntity(HordesCapabilities.HORDESPAWN, type, (entity, ctx) ->
				entity instanceof Mob ? new HordeSpawn.Impl() : null);
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
	public void serverTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || HordeEventConfig.hordesCommandOnly.get()) return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server.overworld();
		if (HordeEventConfig.pauseEventServer.get() && level.players().isEmpty()) return;
		int day = (int) Math.floor(level.getDayTime() / HordeEventConfig.dayLength.get());
		HordeSavedData data = HordeSavedData.getData(level);
		if (day >= data.getNextDay()) data.setNextDay(level.random.nextInt(HordeEventConfig.hordeSpawnVariation.get() + 1)
				+ HordeEventConfig.hordeSpawnDays.get() + data.getNextDay());
		data.save();
	}

	//spawn the horde at the correct time
	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer) || event.player instanceof FakePlayer) return;
		ServerPlayer player = (ServerPlayer) event.player;
		ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
		if (HordeEventConfig.pauseEventServer.get() && level.players().isEmpty()) return;
		HordeEvent horde = HordeSavedData.getData(level).getEvent(player);
		if (horde == null) return;
		if (!horde.hasSynced()) horde.sync(player);
		if (horde.isActive(player)) {
			horde.update(player);
			return;
		}
		int day = horde.getCurrentDay(player);
		int time = Math.round(level.getDayTime() % HordeEventConfig.dayLength.get());
		if (time >= HordeEventConfig.hordeStartTime.get() && day >= horde.getNextDay() && (day > 0 || HordeEventConfig.spawnFirstDay.get())) {
			horde.tryStartEvent(player, HordeEventConfig.hordeSpawnDuration.get(), false);
		}

	}

	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(MobSpawnEvent.AllowDespawn event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null && horde.isActive(player)) event.setResult(Event.Result.DENY);
	}

	//sync entity capabilities when added to level
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void update(LivingEvent.LivingTickEvent event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeSpawn hordespawn = event.getEntity().getCapability(HordesCapabilities.HORDESPAWN);
		if (hordespawn.isSynced()) return;
		Mob entity = (Mob) event.getEntity();
		entity.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null) {
			horde.registerEntity(entity);
			entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player));
		}
		hordespawn.setSynced();
	}

	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		if (HordeEventConfig.canSleepDuringHorde.get() |! (event.getEntity() instanceof ServerPlayer)) return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		ServerLevel level = player.serverLevel();
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde == null) return;
		if (level.isDay() |! (level.dimensionType().bedWorks() && (horde.isHordeDay(player) || horde.isActive(player)))) return;
		event.setResult(BedSleepingProblem.OTHER_PROBLEM);
		player.displayClientMessage(TextUtils.translatableComponent(Constants.hordeTrySleep, "Can't sleep now, a horde is approaching"), true);
	}

}
