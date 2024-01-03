package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.network.GenericStringMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.atlas.api.util.WeightedOutputs;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.*;
import net.smileycorp.hordes.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.data.HordeScriptLoader;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.data.functions.HordeScript;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.hordeevent.network.HordeSoundMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HordeEvent implements IOngoingEvent<Player> {

	private static UUID FOLLOW_RANGE_MODIFIER = UUID.fromString("51cfe045-4248-409e-be37-556d67de4b97");

	private Set<Mob> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private boolean hasChanged = false;

	private HordeSpawnTable loadedTable;

	HordeEvent(HordeSavedData data){
		nextDay = data.getNextDay();
	}

	public void readFromNBT(CompoundTag nbt) {
		entitiesSpawned.clear();
		if (nbt.contains("timer")) {
			timer = nbt.getInt("timer");
		}
		if (nbt.contains("nextDay")) {
			nextDay = nbt.getInt("nextDay");
		}
		if (nbt.contains("day")) {
			day = nbt.getInt("day");
		}
		if (nbt.contains("loadedTable")) {
			loadedTable = HordeTableLoader.INSTANCE.getTable(new ResourceLocation(nbt.getString("loadedTable")));
		}
	}

	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putInt("timer", timer);
		nbt.putInt("nextDay", nextDay);
		nbt.putInt("day", day);
		if (loadedTable != null) nbt.putString("loadedTable", loadedTable.getName().toString());
		hasChanged = false;
		return nbt;
	}

	public void update(Player player) {
		Level level = player.level();
		if (level.isClientSide || player == null || level.dimension() != Level.OVERWORLD) return;
		if (timer % CommonConfigHandler.hordeSpawnInterval.get() == 0) spawnWave(player, getMobCount(player, level));
		timer--;
		if (timer == 0) stopEvent(player, false);
		hasChanged = true;
	}

	private int getMobCount(Player player, Level level) {
		int amount = (int) (CommonConfigHandler.hordeSpawnAmount.get() * (1 + (day / CommonConfigHandler.hordeSpawnDays.get())
				* (CommonConfigHandler.hordeSpawnMultiplier.get() - 1)));
		List<? extends Player> players = level.players();
		for (Player other : players) if (shouldReduce(player, other))
			amount = (int) Math.floor(amount * CommonConfigHandler.hordeMultiplayerScaling.get());
		return amount;
	}

	private boolean shouldReduce(Player player, Player other) {
		if (other == player || player.distanceTo(other) > 25) return false;
		HordeEvent horde = HordeSavedData.getData((ServerLevel) other.level()).getEvent(other);
		return horde != null && horde.isActive(other);
	}

	public void spawnWave(Player player, int count) {
		cleanSpawns();
		HordeSpawnTable table = loadedTable;
		if (table == null) {
			HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeTableLoader.INSTANCE.getFallbackTable(), this);
			postEvent(buildTableEvent);
			table = buildTableEvent.spawntable;
		}
		if (table == null) {
			logError("Cannot load wave spawntable, cancelling spawns.", new Exception());
			return;
		}
		Level level = player.level();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		postEvent(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vec3 basedir = DirectionUtils.getRandomDirectionVecXZ(level.random);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.blockPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(level.random);
			basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
			if (i++ >=20) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basedir = DirectionUtils.getRandomDirectionVecXZ(level.random);
				basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75);
				break;
			}
		}
		WeightedOutputs<HordeSpawnEntry> spawntable = table.getSpawnTable(day);
		if (spawntable.isEmpty()) {
			logInfo("Spawntable is empty, stopping wave spawn.");
			return;
		}
		if (count <= 0) {
			logInfo("Stopping wave spawn because count is " + count);
			return;
		}
		if (player instanceof ServerPlayer) {
			HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new HordeSoundMessage(basedir, startEvent.getSound()),
					((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
		for (int n = 0; n<count; n++) {
			if (entitiesSpawned.size() > CommonConfigHandler.hordeSpawnMax.get()) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3 dir = DirectionUtils.getRandomDirectionVecXZ(level.random);
			BlockPos pos = DirectionUtils.getClosestLoadedPos(level, basepos, dir, level.random.nextInt(10));
			HordeSpawnEntry entry = spawntable.getResult(player.getRandom());
			EntityType<?> type = entry.getEntity();
			try {
				AtomicBoolean cancelled = new AtomicBoolean(false);
				CompoundTag nbt = entry.getNBT();
				nbt.putString("id", entry.getName().toString());
				Mob newEntity = (Mob) EntityType.loadEntityRecursive(nbt, level, (entity)->loadEntity(level, player, (Mob) entity, pos, cancelled));
				if (cancelled.get()) continue;
				newEntity.readAdditionalSaveData(entry.getNBT());
				if (!((ServerLevel)level).tryAddFreshEntityWithPassengers(newEntity)) {
					logError("Unable to spawn entity from " + type, new Exception());
					continue;
				}
				finalizeEntity(newEntity , level, player);
			} catch (Exception e) {
				e.printStackTrace();
				logError("Unable to spawn entity from " + type, e);
			}
		}
	}

	private Entity loadEntity(Level level, Player player, Mob entity, BlockPos pos, AtomicBoolean cancel) {
		HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
		postEvent(spawnEntityEvent);
		if (!spawnEntityEvent.isCanceled()) {
			entity = spawnEntityEvent.entity;
			pos = spawnEntityEvent.pos;
			entity.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(pos), null, null, null);
			entity.setPos(pos.getX(), pos.getY(), pos.getZ());
			return entity;
		} else {
			logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + entity.getType());
			cancel.set(true);
			return entity;
		}
	}

	private void finalizeEntity(Mob entity, Level level, Player player) {
		entity.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER,
				"hordes:horde_range", 75, AttributeModifier.Operation.ADDITION));
		LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (optional.isPresent()) { optional.resolve().get().setPlayerUUID(player.getUUID().toString());
			registerEntity(entity);
			hasChanged = true;
		}
		entity.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
		if (entity instanceof PathfinderMob) entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof Mob) finalizeEntity((Mob) passenger, level, player);
	}

	private void cleanSpawns() {
		List<Mob> toRemove = new ArrayList<>();
		for (Mob entity : entitiesSpawned) {
			if (entity.isAlive() |! entity.isDeadOrDying() |! entity.isRemoved()) continue;
			LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN, null);
			if (optional.isPresent()) {
				HordeSpawn cap = optional.resolve().get();
				cap.setPlayerUUID("");
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}

	public boolean isHordeDay(Player player) {
		Level level = player.level();
		if (level.isClientSide |! (level.dimension() == Level.OVERWORLD)) return false;
		return isActive(player) || Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get()) >= nextDay;
	}

	public boolean isActive(Player player) {
		return timer > 0;
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public void setPlayer(Player player) {
		cleanSpawns();
		entitiesSpawned.forEach(entity->fixGoals(player, entity));
		hasChanged = true;
	}

	private void fixGoals(Player player, Mob entity) {
		for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
			if (!(entry.getGoal() instanceof GoToEntityPositionGoal)) continue;
			entity.goalSelector.removeGoal(entry.getGoal());
			entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
			return;
		}
	}

	public void tryStartEvent(Player player, int duration, boolean isCommand) {
		if (CommonConfigHandler.hordesCommandOnly.get() &! isCommand) return;
		if (player == null) {
			logError("player is null for " + this, new NullPointerException());
			return;
		}
		Level level = player.level();
		if (level.dimension() != Level.OVERWORLD) return;
		HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
		postEvent(startEvent);
		if (startEvent.isCanceled()) {
			loadedTable = null;
			return;
		}
		HordeSpawnTable table = loadedTable;
		if (table == null) {
			HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeTableLoader.INSTANCE.getFallbackTable(), this);
			postEvent(buildTableEvent);
			table = buildTableEvent.spawntable;
		}
		if (!table.getSpawnTable(day).isEmpty()) {
			timer = duration;
			hasChanged = true;
			sendMessage(player, startEvent.getMessage());
			if (isCommand) day = (int) Math.floor(level.getDayTime() / CommonConfigHandler.dayLength.get());
			else day = nextDay;
		} else {
			loadedTable = null;
			logInfo("Spawntable is empty, canceling event start.");
		}
		if (!isCommand) nextDay = HordeSavedData.getData((ServerLevel) level).getNextDay();
	}

	public void setSpawntable(HordeSpawnTable table) {
		loadedTable = table;
	}

	public HordeSpawnTable getSpawntable() {
		return loadedTable;
	}

	public void setNextDay(int day) {
		nextDay = day;
	}

	public int getNextDay() {
		return nextDay;
	}

	private void sendMessage(Player player, String str) {
		HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new GenericStringMessage(str), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	public void stopEvent(Player player, boolean isCommand) {
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand);
		postEvent(endEvent);
		timer = 0;
		loadedTable = null;
		sendMessage(player, endEvent.getMessage());
		for (Mob entity : entitiesSpawned) {
			for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
				if (!(entry.getGoal() instanceof GoToEntityPositionGoal)) continue;
				entity.goalSelector.removeGoal(entry.getGoal());
				break;
			}
			LazyOptional<HordeSpawn> cap = entity.getCapability(HordesCapabilities.HORDESPAWN);
			if (!cap.isPresent()) continue;
			cap.resolve().get().setPlayerUUID("");
			entity.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(FOLLOW_RANGE_MODIFIER);
		}
		hasChanged = true;
	}

	public void removeEntity(Mob entity) {
		entitiesSpawned.remove(entity);
	}

	public void registerEntity(Mob enemy) {
		if (!entitiesSpawned.contains(enemy)) entitiesSpawned.add(enemy);
	}

	public String toString(String player) {
		return "OngoingHordeEvent@" + Integer.toHexString(hashCode()) + "[player = " + (player == null ? "null" : player) + ", isActive = " + (timer > 0) +
				", ticksLeft=" + timer + ", entityCount=" + entitiesSpawned.size()+", nextDay=" + nextDay + ", day=" + day+"]";
	}

	private void logInfo(Object message) {
		HordesLogger.logInfo("[" + this + "]" + message);
	}

	private void logError(Object message, Exception e) {
		HordesLogger.logError("["+this+"]" + message, e);
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
				if (entitylist.indexOf(entity) < entitylist.size()-1) builder.append(", ");
			}
			builder.append("}");
			result.add(builder.toString());
		}
		return result;
	}

	private void postEvent(HordePlayerEvent event) {
		for (HordeScript script : HordeScriptLoader.INSTANCE.getScripts(event)) {
			if (script.shouldApply(event.getEntityWorld(), event.getEntity(), event.getEntityWorld().random)) {
				script.apply(event);
				HordesLogger.logInfo("Applying script " + script.getName());
			}
		}
		MinecraftForge.EVENT_BUS.post(event);
	}

	public void reset(ServerLevel level) {
		entitiesSpawned.clear();
		HordeSavedData data = HordeSavedData.getData((ServerLevel) level);
		nextDay = data.getNextDay();
		loadedTable = null;
		timer = 0;
	}

}
