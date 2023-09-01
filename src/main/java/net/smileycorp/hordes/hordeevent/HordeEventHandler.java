package net.smileycorp.hordes.hordeevent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
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
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

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
		if (event.phase != Phase.START || CommonConfigHandler.hordesCommandOnly.get()) return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server.overworld();
		if (CommonConfigHandler.pauseEventServer.get() && level.players().isEmpty()) return;
		int day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
		HordeSavedData data = HordeSavedData.getData(level);
		if (day >= data.getNextDay()) data.setNextDay(level.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1)
				+ CommonConfigHandler.hordeSpawnDays.get() + data.getNextDay());
		data.save();
	}

	//spawn the horde at the correct time
	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		Player player = event.player;
		if (event.phase != Phase.END || player == null || player instanceof FakePlayer) return;
		Level level = player.level();
		if (level.isClientSide || (CommonConfigHandler.pauseEventServer.get() && level.players().isEmpty())) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) level).getEvent(player);
		if (horde == null) return;
		int day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
		int time = Math.round(level.getDayTime() % CommonConfigHandler.dayLength.get());
		if (horde == null || horde.isActive(player)) return;
		if (time >= CommonConfigHandler.hordeStartTime.get() && day >= horde.getNextDay() && (day>0 || CommonConfigHandler.spawnFirstDay.get())) {
			horde.tryStartEvent(player, CommonConfigHandler.hordeSpawnDuration.get(), false);
		}
		if (horde.isActive(player)) {
			horde.update(player);
		}
	}

	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(MobSpawnEvent.AllowDespawn event) {
		Player player = getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null && horde.isActive(player)) event.setResult(Result.DENY);
	}

	//remove entities from horde when they die
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		Player player = getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null) horde.removeEntity((Mob) event.getEntity());
	}

	//sync entity capabilities when added to level
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void update(LivingTickEvent event) {
		Player player = getHordePlayer(event.getEntity());
		if (player == null) return;
		IHordeSpawn cap = event.getEntity().getCapability(Hordes.HORDESPAWN).resolve().get();
		if (cap.isSynced()) return;
		Mob entity = (Mob) event.getEntity();
		entity.targetSelector.getRunningGoals().forEach((goal) -> goal.stop());
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde != null) {
			horde.registerEntity(entity);
			entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
		}
		cap.setSynced();
	}

	private Player getHordePlayer(Entity entity) {
		if (entity.level().isClientSide |!(entity instanceof Mob)) return null;
		LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN);
		if (!optional.isPresent()) return null;
		IHordeSpawn cap = optional.resolve().get();
		if (!cap.isHordeSpawned()) return null;
		String uuid = cap.getPlayerUUID();
		if (!DataUtils.isValidUUID(uuid)) return null;
		return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
	}

	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		Player player = event.getEntity();
		Level level = player.level();
		if (CommonConfigHandler.canSleepDuringHorde.get() || level.isClientSide) return;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
		if (horde == null) return;
		if (!horde.isHordeDay(player) |! level.dimensionType().bedWorks() || level.isDay() |! horde.isActive(player)) return;
		event.setResult(BedSleepingProblem.OTHER_PROBLEM);
		player.displayClientMessage(TextUtils.translatableComponent(Constants.hordeTrySleep, "Can't sleep now, a horde is approaching"), true);
	}

}
