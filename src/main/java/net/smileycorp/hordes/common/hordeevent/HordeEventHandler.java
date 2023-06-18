package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.nbt.CompoundTag;
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
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;

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
		LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
		if (!optional.isPresent()) return;
		IHordeEvent horde = optional.resolve().get();
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
		LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
		if (optional.isPresent() && optional.resolve().get().isActive(player)) event.setResult(Result.DENY);
	}

	//remove entities from horde when they die
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		Player player = getHordePlayer(event.getEntity());
		if (player == null) return;
		LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
		if (optional.isPresent() && optional.resolve().get().isActive(player)) optional.resolve().get().removeEntity((Mob) event.getEntity());
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
		LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
		if (optional.isPresent()) {
			optional.resolve().get().registerEntity(entity);
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
		LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
		if (!optional.isPresent()) return;
		IHordeEvent horde = optional.resolve().get();
		if (!horde.isHordeDay(player) |! level.dimensionType().bedWorks() || level.isDay() |! horde.isActive(player)) return;
		event.setResult(BedSleepingProblem.OTHER_PROBLEM);
		player.displayClientMessage(TextUtils.translatableComponent(Constants.hordeTrySleep, "Can't sleep now, a horde is approaching"), true);
	}

	//copy horde event capability to new player instance on death
	@SubscribeEvent(receiveCanceled = true)
	public void playerClone(PlayerEvent.Clone event) {
		Player player = event.getEntity();
		Player original = event.getOriginal();
		if (player == null || original == null || player instanceof FakePlayer || original instanceof FakePlayer) return;
		LazyOptional<IHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
		LazyOptional<IHordeEvent> optionalo = original.getCapability(Hordes.HORDE_EVENT, null);
		if (optionalp.isPresent() && optionalo.isPresent()) {
			IHordeEvent horde = optionalp.resolve().get();
			horde.readFromNBT(optionalo.resolve().get().writeToNBT(new CompoundTag()));
			horde.setPlayer(player);
		}
	}

}
