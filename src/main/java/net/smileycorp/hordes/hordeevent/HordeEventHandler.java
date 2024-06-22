package net.smileycorp.hordes.hordeevent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

public class HordeEventHandler {

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(HordeTableLoader.INSTANCE);
		event.addListener(HordeScriptLoader.INSTANCE);
	}

	//update the next day in the horde level data
	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (HordeEventConfig.hordesCommandOnly.get()) return;
		MinecraftServer server = event.getServer();
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
	public void playerTick(PlayerTickEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer) || event.getEntity() instanceof FakePlayer) return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
		if (HordeEventConfig.pauseEventServer.get() && level.players().isEmpty()) return;
		HordeEvent horde = HordeSavedData.getData(level).getEvent(player);
		if (horde == null) return;
		int time = Math.round(level.getDayTime() % HordeEventConfig.dayLength.get());
		int day = horde.getCurrentDay(player);
		if (!horde.hasSynced(day)) horde.sync(player, day);
		if (horde.isActive(player)) {
			horde.update(player);
			return;
		}
		if (time >= HordeEventConfig.hordeStartTime.get() && time <= HordeEventConfig.hordeStartTime.get() + HordeEventConfig.hordeStartBuffer.get()
				&& day >= horde.getNextDay() && (day > 0 || HordeEventConfig.spawnFirstDay.get())) {
			horde.tryStartEvent(player, -1, false);
		}
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			HordeEvent horde = HordeSavedData.getData(player.serverLevel()).getEvent(player);
			if (horde != null) horde.setPlayer(player);
		}
	}
	
	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(MobDespawnEvent event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		player.checkDespawn();
		if (player == null) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null && horde.isActive(player)) event.setResult(MobDespawnEvent.Result.DENY);
	}

	//sync entity capabilities when added to level
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void update(EntityTickEvent event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null |! (event.getEntity() instanceof Mob)) return;
		HordeSpawn cap = event.getEntity().getCapability(HordesCapabilities.HORDESPAWN);
		if (cap.isSynced()) return;
		Mob entity = (Mob) event.getEntity();
		entity.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		if (!(entity instanceof ZombifiedPiglin && CommonConfigHandler.aggressiveZombiePiglins.get())) entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null) if (horde.isActive(player)) horde.registerEntity(entity, player);
		cap.setSynced();
	}

	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(CanPlayerSleepEvent event) {
		if (HordeEventConfig.canSleepDuringHorde.get() || !(event.getEntity() instanceof ServerPlayer)) return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		ServerLevel level = player.serverLevel();
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde == null) return;
		if (level.isDay() |! (level.dimensionType().bedWorks() && (horde.isHordeDay(player) || horde.isActive(player)))) return;
		event.setProblem(BedSleepingProblem.OTHER_PROBLEM);
		player.displayClientMessage(TextUtils.translatableComponent(Constants.hordeTrySleep, "Can't sleep now, a horde is approaching"), true);
	}

}
