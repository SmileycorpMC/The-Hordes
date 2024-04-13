package net.smileycorp.hordes.hordeevent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

public class HordeEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Mob) {
			event.addCapability(Constants.loc("HordeSpawn"), new HordeSpawn.Provider());
		}
		if (entity instanceof Player && entity.level().isClientSide) {
			event.addCapability(Constants.loc("HordeEventClient"), new HordeEventClient.Provider());
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
		if (event.phase != Phase.START || HordeEventConfig.hordesCommandOnly.get()) return;
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
	public void playerTick(PlayerTickEvent event) {
		if (event.phase != Phase.END || !(event.player instanceof ServerPlayer) || event.player instanceof FakePlayer) return;
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
		if (time >= HordeEventConfig.hordeStartTime.get() && time <= HordeEventConfig.hordeStartTime.get() + HordeEventConfig.hordeStartBuffer.get()
				&& day >= horde.getNextDay() && (day > 0 || HordeEventConfig.spawnFirstDay.get())) {
			horde.tryStartEvent(player, -1, false);
		}

	}

	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(MobSpawnEvent.AllowDespawn event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null && horde.isActive(player)) event.setResult(Result.DENY);
	}

	//sync entity capabilities when added to level
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void update(LivingTickEvent event) {
		ServerPlayer player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeSpawn cap = event.getEntity().getCapability(HordesCapabilities.HORDESPAWN).orElseGet(null);
		if (cap.isSynced()) return;
		Mob entity = (Mob) event.getEntity();
		entity.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null) if (horde.isActive(player)) horde.registerEntity(entity, player);
		cap.setSynced();
	}

	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		if (HordeEventConfig.canSleepDuringHorde.get() || !(event.getEntity() instanceof ServerPlayer)) return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		ServerLevel level = player.serverLevel();
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde == null) return;
		if (level.isDay() |! (level.dimensionType().bedWorks() && (horde.isHordeDay(player) || horde.isActive(player)))) return;
		event.setResult(BedSleepingProblem.OTHER_PROBLEM);
		player.displayClientMessage(TextUtils.translatableComponent(Constants.hordeTrySleep, "Can't sleep now, a horde is approaching"), true);
	}

}
