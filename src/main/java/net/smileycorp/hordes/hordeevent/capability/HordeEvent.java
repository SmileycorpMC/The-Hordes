package net.smileycorp.hordes.hordeevent.capability;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.GenericStringMessage;
import net.smileycorp.atlas.api.util.VecMath;
import net.smileycorp.atlas.api.util.WeightedOutputs;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.ai.HordeTrackPlayerGoal;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.*;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.hordeevent.network.HordeSoundMessage;
import net.smileycorp.hordes.hordeevent.network.UpdateClientHordeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class HordeEvent {

	private static ResourceLocation FOLLOW_RANGE_MODIFIER = Constants.loc("horde_range");
	private final RandomSource rand;
	private Set<Mob> entitiesSpawned = Sets.newHashSet();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private HordeSpawnData spawnData = null;
	int sentDay = 0;
	private String username;

	HordeEvent(HordeSavedData data){
		nextDay = HordeEventConfig.hordeEventByPlayerTime.get() ? HordeEventConfig.spawnFirstDay.get() ? 0 : HordeEventConfig.hordeSpawnDays.get()
				: data.getNextDay();
		rand = data.getRandom();
	}

	public void readFromNBT(CompoundTag nbt) {
		entitiesSpawned.clear();
		if (nbt.contains("timer")) timer = nbt.getInt("timer");
		if (nbt.contains("nextDay")) nextDay = nbt.getInt("nextDay");
		if (nbt.contains("day")) day = nbt.getInt("day");
		if (nbt.contains("spawnData")) spawnData = new HordeSpawnData(this, nbt.getCompound("spawnData"));
		if (nbt.contains("loadedTable")) {
			spawnData = new HordeSpawnData(this);
			spawnData.setTable(HordeTableLoader.INSTANCE.getTable(ResourceLocation.tryParse(nbt.getString("loadedTable"))));
		}
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt, UUID uuid) {
		nbt.putInt("timer", timer);
		nbt.putInt("nextDay", nextDay);
		nbt.putInt("day", day);
		if (spawnData != null) nbt.put("spawnData", spawnData.save());
		ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
		nbt.putString("username", player == null ? username == null ? uuid.toString() : username : player.getName().getString());
		return nbt;
	}
	
	public void update(ServerPlayer player) {
		Level level = player.level();
		if (level.dimension() != Level.OVERWORLD) return;
		if (spawnData == null) return;
		if (timer % spawnData.getSpawnInterval() == 0) spawnWave(player, getMobCount(player, level));
		timer--;
		if (timer == 0) stopEvent(player, false);
	}

	private int getMobCount(ServerPlayer player, Level level) {
		int amount = spawnData.getSpawnAmount();
		List<? extends Player> players = level.players();
		for (Player other : players) if (shouldReduce(player, (ServerPlayer) other))
			amount = (int) Math.floor(amount * HordeEventConfig.hordeMultiplayerScaling.get());
		return amount;
	}

	private boolean shouldReduce(ServerPlayer player, ServerPlayer other) {
		if (other == player || player.distanceTo(other) > 25) return false;
		HordeEvent horde = HordeSavedData.getData(other.serverLevel()).getEvent(other);
		return horde != null && horde.isActive(other);
	}

	public void spawnWave(ServerPlayer player, int count) {
		cleanSpawns();
		if (spawnData == null) {
			HordeBuildSpawnDataEvent buildTableEvent = new HordeBuildSpawnDataEvent(player, this);
			postEvent(buildTableEvent);
			spawnData = buildTableEvent.getSpawnData();
		}
		if (spawnData == null || spawnData.getTable() == null) {
			logError("Cannot load wave spawn data, cancelling spawns.", new Exception());
			return;
		}
		ServerLevel level = player.serverLevel();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		postEvent(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vec3 basedir = VecMath.randomXZVec(rand);
		BlockPos basepos = VecMath.closestLoadedPos(level, player.blockPosition(), basedir, 50, 7, 0);
		int i = 0;
		while (basepos.equals(player.blockPosition())) {
			basedir = VecMath.randomXZVec(rand);
			basepos = VecMath.closestLoadedPos(level, player.blockPosition(), basedir, 50, 7, 0);
			if (!spawnData.getSpawnType().canSpawn(level, basepos)) basepos = player.blockPosition();
			if (i++ >= HordeEventConfig.hordeSpawnChecks.get()) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basedir = VecMath.randomXZVec(rand);
				basepos = VecMath.closestLoadedPos(level, player.blockPosition(), basedir, 75);
				break;
			}
		}
		WeightedOutputs<HordeSpawnEntry> spawntable = spawnData.getTable().getSpawnTable(day);
		if (spawntable.isEmpty()) {
			logInfo("Spawntable is empty, stopping wave spawn.");
			return;
		}
		if (count <= 0) {
			logInfo("Stopping wave spawn because count is " + count);
			return;
		}
		HordeEventPacketHandler.sendTo(new HordeSoundMessage(basedir, spawnData.getSpawnSound()), player);
		for (HordeSpawnEntry entry : spawntable.getResults(rand, count)) {
			if (entitiesSpawned.size() > HordeEventConfig.hordeSpawnMax.get()) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3 pos = getSpawnPos(level, basepos.getCenter());
			EntityType<?> type = entry.getEntity();
			try {
				AtomicBoolean cancelled = new AtomicBoolean(false);
				CompoundTag nbt = entry.getNBT();
				nbt.putString("id", entry.getName().toString());
				Mob newEntity = (Mob) EntityType.loadEntityRecursive(nbt, level, (entity) -> loadEntity(level, player, (Mob) entity, pos, cancelled));
				if (cancelled.get()) continue;
				newEntity.readAdditionalSaveData(entry.getNBT());
				if (!(level.tryAddFreshEntityWithPassengers(newEntity))) {
					logError("Unable to spawn entity from " + type, new Exception());
					continue;
				}
				finalizeEntity(newEntity, player);
			} catch (Exception e) {
				e.printStackTrace();
				logError("Unable to spawn entity from " + type, e);
			}
		}
	}
	
	private Vec3 getSpawnPos(ServerLevel level, Vec3 basepos) {
		for (int j = 0; j < 5; j++) {
			double x = basepos.x() + rand.nextInt(10);
			double z = basepos.z() + rand.nextInt(10);
			Vec3 pos = new Vec3(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z), z);
			if (spawnData.getSpawnType().canSpawn(level, BlockPos.containing(pos))) return pos;
		}
		return basepos;
	}
	
	private Entity loadEntity(ServerLevel level, ServerPlayer player, Mob entity, Vec3 pos, AtomicBoolean cancel) {
		HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
		postEvent(spawnEntityEvent);
		if (!spawnEntityEvent.isCanceled()) {
			entity = spawnEntityEvent.getEntity();
			pos = spawnEntityEvent.getPos();
			entity.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)), null, null);
			entity.setPos(pos.x(), pos.y(), pos.z());
			return entity;
		} else {
			logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + entity.getType());
			cancel.set(true);
			return entity;
		}
	}

	private void finalizeEntity(Mob entity, ServerPlayer player) {
		entity.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER,
				75, AttributeModifier.Operation.ADD_VALUE));
		HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (cap != null) {
			cap.setPlayerUUID(player.getUUID().toString());
			registerEntity(entity, player);
		}
		entity.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, ServerPlayer.class, true));
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof Mob) finalizeEntity((Mob) passenger, player);
	}

	private void cleanSpawns() {
		List<Mob> toRemove = new ArrayList<>();
		for (Mob entity : entitiesSpawned) {
			if (entity.isAlive() |! entity.isRemoved()) continue;
			HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
			if (cap != null) {
				cap.setPlayerUUID("");
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}

	public boolean isHordeDay(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		if (level.dimension() != Level.OVERWORLD) return false;
		return isActive(player) || getCurrentDay(player) >= nextDay;
	}

	public boolean isActive(ServerPlayer player) {
		return timer > 0;
	}
	
	public void setPlayer(ServerPlayer player) {
		setNextDay(player);
		cleanSpawns();
		entitiesSpawned.forEach(entity -> fixGoals(player, entity));
	}

	private void fixGoals(ServerPlayer player, Mob entity) {
		for (WrappedGoal entry : entity.goalSelector.getAvailableGoals().toArray(WrappedGoal[]::new)) {
			if (!(entry.getGoal() instanceof HordeTrackPlayerGoal)) continue;
			entity.goalSelector.removeGoal(entry.getGoal());
			entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player, spawnData.getEntitySpeed()));
			return;
		}
	}

	public void tryStartEvent(ServerPlayer player, int duration, boolean isCommand) {
		cleanSpawns();
		if (HordeEventConfig.hordesCommandOnly.get() &! isCommand) return;
		if (!isCommand) {
			logInfo("Trying to start horde event on day " + getCurrentDay(player) + " with nextDay " + nextDay + " and time "
					+ player.level().getDayTime() % HordeEventConfig.dayLength.get());
		}
		if (player == null) {
			logError("player is null for " + this, new NullPointerException());
			return;
		}
		ServerLevel level = player.serverLevel();
		if (level.dimension() != Level.OVERWORLD) return;
		HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
		postEvent(startEvent);
		if (startEvent.isCanceled()) {
			spawnData = null;
			return;
		}
		if (spawnData == null) {
			HordeBuildSpawnDataEvent event = new HordeBuildSpawnDataEvent(player, this);
			postEvent(event);
			spawnData = event.getSpawnData();
		}
		if (spawnData == null || spawnData.getTable() == null || spawnData.getTable().getSpawnTable(day).isEmpty()) {
			spawnData = null;
			logInfo("Spawntable is empty, canceling event start.");
		}
		else {
			timer = duration <= 0 ? spawnData.getSpawnDuration() : duration;
			sendMessage(player, spawnData.getStartMessage());
			if (isCommand) day = getCurrentDay(player);
			else day = nextDay;
		}
		if (!isCommand) setNextDay(player);
	}

	public void setSpawntable(HordeSpawnTable table) {
		if (table == null || table == HordeTableLoader.INSTANCE.getFallbackTable()) {
			spawnData = null;
			return;
		}
		if (spawnData == null) spawnData = new HordeSpawnData(this);
		spawnData.setTable(table);
	}

	public HordeSpawnTable getSpawntable() {
		return spawnData == null ? null : spawnData.getTable();
	}

	public void setNextDay(int day) {
		nextDay = day;
	}

	public int getNextDay() {
		return nextDay;
	}

	private void sendMessage(ServerPlayer player, String str) {
		HordeEventPacketHandler.sendTo(new GenericStringMessage(str, HordeEventPacketHandler.NOTIFICATION), player);
	}

	public void stopEvent(ServerPlayer player, boolean isCommand) {
		entitiesSpawned.clear();
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand, spawnData.getEndMessage());
		postEvent(endEvent);
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(false), player);
		sentDay = getCurrentDay(player);
		timer = 0;
		spawnData = null;
		sendMessage(player, endEvent.getMessage());
		for (Mob entity : entitiesSpawned) {
			for (WrappedGoal entry : entity.goalSelector.getAvailableGoals().toArray(WrappedGoal[]::new)) {
				if (!(entry.getGoal() instanceof HordeTrackPlayerGoal)) continue;
				entity.goalSelector.removeGoal(entry.getGoal());
				break;
			}
			HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN);
			if (cap != null) continue;
			cap.setPlayerUUID("");
			entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(FOLLOW_RANGE_MODIFIER);
		}
	}

	public void removeEntity(Mob entity) {
		entitiesSpawned.remove(entity);
	}

	public void registerEntity(Mob entity, ServerPlayer player) {
		if (!isActive(player) || spawnData == null) {
			HordeSpawn cap = entity.getCapability(HordesCapabilities.HORDESPAWN);
			if (cap != null) cap.setPlayerUUID("");
			return;
		}
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
		entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player, spawnData.getEntitySpeed()));
	}

	private void postEvent(HordePlayerEvent event) {
		HordeScriptLoader.INSTANCE.applyScripts(event);
		NeoForge.EVENT_BUS.post(event);
	}
	
	public void reset(ServerPlayer player) {
		entitiesSpawned.clear();
		setNextDay(player);
		spawnData = null;
		timer = 0;
	}
	
	private void setNextDay(ServerPlayer player) {
		if (!HordeEventConfig.hordeEventByPlayerTime.get()) {
			nextDay = HordeSavedData.getData(player.serverLevel()).getNextDay();
			return;
		}
		int expectedDay = HordeEventConfig.hordeSpawnDays.get() * ((getCurrentDay(player) / HordeEventConfig.hordeSpawnDays.get()) + 1);
		if (nextDay <= getCurrentDay(player) || Math.abs(nextDay - expectedDay) > HordeEventConfig.hordeSpawnDays.get() + HordeEventConfig.hordeSpawnVariation.get()) {
			if (HordeEventConfig.hordeSpawnVariation.get() > 0) expectedDay += rand.nextInt(HordeEventConfig.hordeSpawnVariation.get());
			nextDay = expectedDay;
		}
	}
	
	public boolean hasSynced(int day) {
		return sentDay >= day;
	}
	
	public void sync(ServerPlayer player, int day) {
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(isHordeDay(player)), player);
		sentDay = day;
	}
	
	public int getDay() {
		return day;
	}

	public int getCurrentDay(ServerPlayer player) {
		return (int) Math.floor((HordeEventConfig.hordeEventByPlayerTime.get() ? player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
				: player.level().getDayTime()) / HordeEventConfig.dayLength.get());
	}

	private void logInfo(Object message) {
		HordesLogger.logInfo("[" + this + "]" + message);
	}

	private void logError(Object message, Exception e) {
		HordesLogger.logError("["+this+"]" + message, e);
	}

	public String toString(String player) {
		return "OngoingHordeEvent@" + Integer.toHexString(hashCode()) + "[player = " + (player == null ? "null" : player) + ", isActive = " + (timer > 0) +
				", ticksLeft=" + timer + ", entityCount=" + entitiesSpawned.size()+", nextDay=" + nextDay + ", day=" + day+"]";
	}

	public List<String> getEntityStrings() {
		List<String> result = new ArrayList<>();
		result.add("	entities: {" + (entitiesSpawned.isEmpty() ? "}" : ""));
		List<Mob> entitylist = new ArrayList<>(entitiesSpawned);
		for (int i = 0; i < entitylist.size(); i += 10) {
			List<Mob> sublist = entitylist.subList(i, Math.min(i+9, entitylist.size()-1));
			StringBuilder builder = new StringBuilder();
			builder.append("		");
			for (Mob entity : sublist) {
				builder.append(entity.getClass().getSimpleName() + "@");
				builder.append(Integer.toHexString(entity.hashCode()));
				if (entitylist.indexOf(entity) < entitylist.size() -1) builder.append(", ");
			}
			builder.append("}");
			result.add(builder.toString());
		}
		return result;
	}
	
	public RandomSource getRandom() {
		return rand;
	}
	
}