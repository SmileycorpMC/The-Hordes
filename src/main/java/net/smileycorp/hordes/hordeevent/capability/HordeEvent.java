package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HordeEvent implements IOngoingEvent<ServerPlayerEntity> {
	
	private static UUID FOLLOW_RANGE_MODIFIER = UUID.fromString("51cfe045-4248-409e-be37-556d67de4b97");
	private final Random rand;
	private Set<MobEntity> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private HordeSpawnData spawnData = null;
	boolean sentDay;
	
	HordeEvent(HordeSavedData data){
		nextDay = HordeEventConfig.hordeEventByPlayerTime.get() ? HordeEventConfig.spawnFirstDay.get() ? 0 :HordeEventConfig.hordeSpawnDays.get()
				: data.getNextDay();
		rand = data.getRandom();
	}
	
	public void readFromNBT(CompoundNBT nbt) {
		entitiesSpawned.clear();
		if (nbt.contains("timer")) timer = nbt.getInt("timer");
		if (nbt.contains("nextDay")) nextDay = nbt.getInt("nextDay");
		if (nbt.contains("day")) day = nbt.getInt("day");
		if (nbt.contains("spawnData")) spawnData = new HordeSpawnData(this, nbt.getCompound("spawnData"));
		if (nbt.contains("loadedTable")) {
			spawnData = new HordeSpawnData(this);
			spawnData.setTable(HordeTableLoader.INSTANCE.getTable(new ResourceLocation(nbt.getString("loadedTable"))));
		}
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt.putInt("timer", timer);
		nbt.putInt("nextDay", nextDay);
		nbt.putInt("day", day);
		if (spawnData != null) nbt.put("spawnData", spawnData.save());
		return nbt;
	}
	
	public void update(ServerPlayerEntity player) {
		World level = player.level;
		if (level.dimension() != World.OVERWORLD) return;
		if (spawnData == null) return;
		if (timer % spawnData.getSpawnInterval() == 0) spawnWave(player, getMobEntityCount(player, level));
		timer--;
		if (timer == 0) stopEvent(player, false);
	}
	
	private int getMobEntityCount(ServerPlayerEntity player, World level) {
		int amount = spawnData.getSpawnAmount();
		List<? extends PlayerEntity> players = level.players();
		for (PlayerEntity other : players) if (shouldReduce(player, (ServerPlayerEntity) other))
			amount = (int) Math.floor(amount * HordeEventConfig.hordeMultiplayerScaling.get());
		return amount;
	}
	
	private boolean shouldReduce(ServerPlayerEntity player, ServerPlayerEntity other) {
		if (other == player || player.distanceTo(other) > 25) return false;
		HordeEvent horde = HordeSavedData.getData(other.getLevel()).getEvent(other);
		return horde != null && horde.isActive(other);
	}
	
	public void spawnWave(ServerPlayerEntity player, int count) {
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
		ServerWorld level = player.getLevel();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		postEvent(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vector3d basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.blockPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
			basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
			if (!spawnData.getSpawnType().canSpawn(level, basepos)) basepos = player.blockPosition();
			if (i++ >= HordeEventConfig.hordeSpawnChecks.get()) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
				basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75);
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
		HordeEventPacketHandler.sendTo(new HordeSoundMessage(basedir, spawnData.getSpawnSound()),
				player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		for (HordeSpawnEntry entry : spawntable.getResults(rand, count)) {
			if (entitiesSpawned.size() > HordeEventConfig.hordeSpawnMax.get()) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vector3d pos = getSpawnPos(level, Vector3d.atCenterOf(basepos));
			EntityType<?> type = entry.getEntity();
			try {
				AtomicBoolean cancelled = new AtomicBoolean(false);
				CompoundNBT nbt = entry.getNBT();
				nbt.putString("id", entry.getName().toString());
				MobEntity newEntity = (MobEntity) EntityType.loadEntityRecursive(nbt, level, (entity) -> loadEntity(level, player, (MobEntity) entity, pos, cancelled));
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
	
	private Vector3d getSpawnPos(ServerWorld level, Vector3d basepos) {
		for (int j = 0; j < 5; j++) {
			double x = basepos.x() + rand.nextInt(10);
			double z = basepos.z() + rand.nextInt(10);
			Vector3d pos = new Vector3d(x, level.getHeight(Heightmap.Type.MOTION_BLOCKING, (int)x, (int)z), z);
			if (spawnData.getSpawnType().canSpawn(level, new BlockPos(pos))) return pos;
		}
		return basepos;
	}
	
	private Entity loadEntity(ServerWorld level, ServerPlayerEntity player, MobEntity entity, Vector3d pos, AtomicBoolean cancel) {
		HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
		postEvent(spawnEntityEvent);
		if (!spawnEntityEvent.isCanceled()) {
			entity = spawnEntityEvent.getEntity();
			pos = spawnEntityEvent.getPos();
			entity.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(pos)), null, null, null);
			entity.setPos(pos.x(), pos.y(), pos.z());
			return entity;
		} else {
			logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + entity.getType());
			cancel.set(true);
			return entity;
		}
	}
	
	private void finalizeEntity(MobEntity entity, ServerPlayerEntity player) {
		entity.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER,
				"hordes:horde_range", 75, AttributeModifier.Operation.ADDITION));
		LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (optional.isPresent()) {
			optional.orElseGet(null).setPlayerUUID(player.getUUID().toString());
			registerEntity(entity, player);
		}
		entity.targetSelector.getRunningGoals().forEach(PrioritizedGoal::stop);
		if (entity instanceof CreatureEntity) entity.targetSelector.addGoal(1, new HurtByTargetGoal((CreatureEntity) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, ServerPlayerEntity.class, true));
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof MobEntity) finalizeEntity((MobEntity) passenger, player);
	}
	
	private void cleanSpawns() {
		List<MobEntity> toRemove = new ArrayList<>();
		for (MobEntity entity : entitiesSpawned) {
			if (entity.isAlive()) continue;
			LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
			if (optional.isPresent()) {
				HordeSpawn cap = optional.orElseGet(null);
				cap.setPlayerUUID("");
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}
	
	public boolean isHordeDay(ServerPlayerEntity player) {
		ServerWorld level = player.getLevel();
		if (level.dimension() != World.OVERWORLD) return false;
		return isActive(player) || getCurrentDay(player) >= nextDay;
	}
	
	public boolean isActive(ServerPlayerEntity player) {
		return timer > 0;
	}
	
	public void setPlayer(ServerPlayerEntity player) {
		cleanSpawns();
		entitiesSpawned.forEach(entity -> fixGoals(player, entity));
	}
	
	private void fixGoals(ServerPlayerEntity player, MobEntity entity) {
		for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
			if (!(entry.getGoal() instanceof HordeTrackPlayerGoal)) continue;
			entity.goalSelector.removeGoal(entry.getGoal());
			entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player, spawnData.getEntitySpeed()));
			return;
		}
	}
	
	public void tryStartEvent(ServerPlayerEntity player, int duration, boolean isCommand) {
		cleanSpawns();
		if (HordeEventConfig.hordesCommandOnly.get() &! isCommand) return;
		if (!isCommand) {
			logInfo("Trying to start horde event on day " + getCurrentDay(player) + " with nextDay " + nextDay + " and time "
					+ player.level.getDayTime() % HordeEventConfig.dayLength.get());
		}
		if (player == null) {
			logError("player is null for " + this, new NullPointerException());
			return;
		}
		ServerWorld level = player.getLevel();
		if (level.dimension() != World.OVERWORLD) return;
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
		if (!isCommand) nextDay = HordeEventConfig.hordeEventByPlayerTime.get() ? nextDay + HordeEventConfig.hordeSpawnDays.get()
				: HordeSavedData.getData(level).getNextDay();
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
	
	private void sendMessage(ServerPlayerEntity player, String str) {
		HordeEventPacketHandler.sendTo(new SimpleStringMessage(str), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public void stopEvent(ServerPlayerEntity player, boolean isCommand) {
		entitiesSpawned.clear();
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand, spawnData.getEndMessage());
		postEvent(endEvent);
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(nextDay, 0),
				player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		timer = 0;
		spawnData = null;
		sendMessage(player, endEvent.getMessage());
		for (MobEntity entity : entitiesSpawned) {
			for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
				if (!(entry.getGoal() instanceof HordeTrackPlayerGoal)) continue;
				entity.goalSelector.removeGoal(entry.getGoal());
				break;
			}
			LazyOptional<HordeSpawn> cap = entity.getCapability(HordesCapabilities.HORDESPAWN);
			if (!cap.isPresent()) continue;
			cap.orElseGet(null).setPlayerUUID("");
			entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(FOLLOW_RANGE_MODIFIER);
		}
	}
	
	public void removeEntity(MobEntity entity) {
		entitiesSpawned.remove(entity);
	}
	
	public void registerEntity(MobEntity entity, ServerPlayerEntity player) {
		if (!isActive(player) || spawnData == null) {
			LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN);
			if (optional.isPresent()) optional.orElseGet(null).setPlayerUUID("");
			return;
		}
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
		entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player, spawnData.getEntitySpeed()));
	}
	
	private void postEvent(HordePlayerEvent event) {
		HordeScriptLoader.INSTANCE.applyScripts(event);
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	public void reset(ServerWorld level) {
		entitiesSpawned.clear();
		HordeSavedData data = HordeSavedData.getData(level);
		nextDay = data.getNextDay();
		spawnData = null;
		timer = 0;
	}
	
	public boolean hasSynced() {
		return sentDay;
	}
	
	public void sync(ServerPlayerEntity player) {
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(isActive(player) ? day : nextDay, HordeEventConfig.dayLength.get()),
				player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		sentDay = true;
	}
	
	public int getDay() {
		return day;
	}
	
	public int getCurrentDay(ServerPlayerEntity player) {
		return (int) Math.floor((HordeEventConfig.hordeEventByPlayerTime.get() ? player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_ONE_MINUTE))
				: player.level.getDayTime()) / HordeEventConfig.dayLength.get());
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
		List<MobEntity> entitylist = new ArrayList<>(entitiesSpawned);
		for (int i = 0; i < entitylist.size(); i += 10) {
			List<MobEntity> sublist = entitylist.subList(i, Math.min(i+9, entitylist.size()-1));
			StringBuilder builder = new StringBuilder();
			builder.append("		");
			for (MobEntity entity : sublist) {
				builder.append(entity.getClass().getSimpleName() + "@");
				builder.append(Integer.toHexString(entity.hashCode()));
				if (entitylist.indexOf(entity) < entitylist.size() -1) builder.append(", ");
			}
			builder.append("}");
			result.add(builder.toString());
		}
		return result;
	}
	
	public Random getRandom() {
		return rand;
	}
	
}
