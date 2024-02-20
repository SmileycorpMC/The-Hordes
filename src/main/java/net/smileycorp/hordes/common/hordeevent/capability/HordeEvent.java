package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.ai.HordeTrackPlayerGoal;
import net.smileycorp.hordes.common.event.*;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.common.hordeevent.data.scripts.HordeScript;
import net.smileycorp.hordes.common.hordeevent.data.scripts.HordeScriptLoader;
import net.smileycorp.hordes.common.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.common.hordeevent.network.HordeSoundMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class HordeEvent implements IHordeEvent {

	private Set<Mob> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private boolean hasChanged = false;
	private Random rand = new Random();

	private HordeSpawnTable loadedTable;

	public HordeEvent(){
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			HordeSavedData data = HordeSavedData.getData(ServerLifecycleHooks.getCurrentServer().overworld());
			nextDay = data.getNextDay();
		}
	}

	@Override
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

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putInt("timer", timer);
		nbt.putInt("nextDay", nextDay);
		nbt.putInt("day", day);
		if (loadedTable != null) nbt.putString("loadedTable", loadedTable.getName().toString());
		hasChanged = false;
		return nbt;
	}

	@Override
	public void update(Player player) {
		Level level = player.level;
		if (!level.isClientSide && player!=null) {
			if (level.dimension() == Level.OVERWORLD) {
				if ((timer % CommonConfigHandler.hordeSpawnInterval.get()) == 0) {
					int amount = (int)(CommonConfigHandler.hordeSpawnAmount.get() * (1+(day/CommonConfigHandler.hordeSpawnDays.get()) * (CommonConfigHandler.hordeSpawnMultiplier.get()-1)));
					List<? extends Player>players = level.players();
					for (Player entity : players) {
						if (entity != player && player.distanceTo(entity)<=25) {
							amount = (int) Math.floor(amount * CommonConfigHandler.hordeMultiplayerScaling.get());
						}
					}
					spawnWave(player, amount);
				}
				timer--;
				if (timer == 0) {
					stopEvent(player, false);
				}
				hasChanged = true;
			}
		}
	}

	@Override
	public void spawnWave(Player player, int count) {
		cleanSpawns();
		HordeSpawnTable table = loadedTable;
		if (table == null) {
			HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeTableLoader.INSTANCE.getDefaultTable(), this);
			postEvent(buildTableEvent);
			table = buildTableEvent.spawntable;
		}
		if (table == null) {
			logError("Cannot load wave spawntable, cancelling spawns.", new Exception());
			return;
		}
		Level level = player.level;
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		postEvent(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vec3 basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.blockPosition()) |! level.getBlockState(basepos.below()).getMaterial().isSolid()) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
			basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75, 7, 0);
			i++;
			if (i==20) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basepos = DirectionUtils.getClosestLoadedPos(level, player.blockPosition(), basedir, 75);
				break;
			}
		}
		WeightedOutputs<HordeSpawnEntry> spawntable = table.getSpawnTable(day);
		if (spawntable.isEmpty()) {
			logInfo("Spawntable is empty, stopping wave spawn.");
			return;
		}
		if (count > 0) {
			if (player instanceof ServerPlayer) {
				HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new HordeSoundMessage(basedir, startEvent.getSound()), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		} else {
			logInfo("Stopping wave spawn because count is " + count);
		}
		for (int n = 0; n<count; n++) {
			if (entitiesSpawned.size() > CommonConfigHandler.hordeSpawnMax.get()) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3 dir = DirectionUtils.getRandomDirectionVecXZ(rand);
			BlockPos pos = DirectionUtils.getClosestLoadedPos(level, basepos, dir, rand.nextInt(10));
			HordeSpawnEntry entry = spawntable.getResult(rand);
			EntityType<?> type = entry.getEntity();
			try {
				AtomicBoolean cancelled = new AtomicBoolean(false);
				CompoundTag nbt = entry.getNBT();
				nbt.putString("id", entry.getName().toString());
				Mob newEntity = (Mob) EntityType.loadEntityRecursive(nbt, level, (entity)->{
					Entity e = loadEntity(level, player, (Mob) entity, pos);
					if (e instanceof Player) {
						cancelled.set(true);
						return null;
					}
					return e;
				});
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

	private Entity loadEntity(Level level, Player player, Mob entity, BlockPos pos) {
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
			return player;
		}
	}

	private void finalizeEntity(Mob entity, Level level, Player player) {
		entity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(100.0D);
		LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN);
		if (optional.isPresent()) { optional.resolve().get().setPlayerUUID(player.getUUID().toString());
			registerEntity(entity);
			hasChanged = true;
		}
		entity.targetSelector.getRunningGoals().forEach((goal) -> goal.stop());
		if (entity instanceof PathfinderMob) {
			entity.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) entity));
		}
		entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
		entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player)); //TODO: debug ai not working correctly on drowned mobs that aren't in water
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof Mob) finalizeEntity((Mob) passenger, level, player);
	}

	private void cleanSpawns() {
		List<Mob> toRemove = new ArrayList<>();
		for (Mob entity : entitiesSpawned) {
			if (entity.isDeadOrDying() |! entity.isAlive()) {
				LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
				if (optional.isPresent()) {
					IHordeSpawn cap = optional.resolve().get();
					cap.setPlayerUUID("");
					toRemove.add(entity);
				}
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}

	@Override
	public boolean isHordeDay(Player player) {
		Level level = player.level;
		if (level.isClientSide |!(level.dimension() == Level.OVERWORLD)) return false;
		return isActive(player) || (!CommonConfigHandler.hordesCommandOnly.get() && Math.floor(level.getDayTime()/CommonConfigHandler.dayLength.get())>=nextDay);
	}

	@Override
	public boolean isActive(Player player) {
		return timer > 0;
	}

	@Override
	public boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public void setPlayer(Player player) {
		Set<Mob> toRemove = new HashSet<>();
		for (Mob entity : entitiesSpawned) {
			if (entity!=null) {
				GoToEntityPositionGoal task = null;
				for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
					if (entry.getGoal() instanceof GoToEntityPositionGoal) {
						task = (GoToEntityPositionGoal) entry.getGoal();
						break;
					}
				}
				if (task != null) {
					entity.goalSelector.removeGoal(task);
					entity.goalSelector.addGoal(6, new HordeTrackPlayerGoal(entity, player));
				}
			} else toRemove.add(entity);
		}
		entitiesSpawned.removeAll(toRemove);
		hasChanged = true;
	}

	@Override
	public void tryStartEvent(Player player, int duration, boolean isCommand) {
		if (CommonConfigHandler.hordesCommandOnly.get()) return;
		if (player!=null) {
			Level level = player.level;
			if (level.dimension() == Level.OVERWORLD) {
				HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
				postEvent(startEvent);
				if (startEvent.isCanceled()) {
					loadedTable = null;
					return;
				}
				HordeSpawnTable table = loadedTable;
				if (table == null) {
					HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeTableLoader.INSTANCE.getDefaultTable(), this);
					postEvent(buildTableEvent);
					table = buildTableEvent.spawntable;
				}
				if (!table.getSpawnTable(day).isEmpty()) {
					timer = duration;
					hasChanged = true;
					sendMessage(player, startEvent.getMessage());
					if (isCommand) day = (int) Math.floor(level.getDayTime()/CommonConfigHandler.dayLength.get());
					else day = nextDay;
				} else {
					loadedTable = null;
					logInfo("Spawntable is empty, canceling event start.");
				}
				if (!isCommand) nextDay = HordeSavedData.getData((ServerLevel) level).getNextDay();
			}
		} else logError("player is null for " + toString(), new NullPointerException());
	}

	public void setSpawntable(HordeSpawnTable table) {
		loadedTable = table;
	}

	public HordeSpawnTable getSpawntable() {
		return loadedTable;
	}

	@Override
	public void setNextDay(int day) {
		nextDay=day;
	}

	@Override
	public int getNextDay() {
		return nextDay;
	}

	private void sendMessage(Player player, String str) {
		HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(str), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	@Override
	public void stopEvent(Player player, boolean isCommand) {
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand);
		postEvent(endEvent);
		timer = 0;
		cleanSpawns();
		sendMessage(player, endEvent.getMessage());
		hasChanged = true;
		loadedTable = null;
	}

	@Override
	public void removeEntity(Mob entity) {
		entitiesSpawned.remove(entity);
	}

	@Override
	public void registerEntity(Mob entity) {
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
	}

	public String toString(Player player) {
		return "OngoingHordeEvent@" + Integer.toHexString(hashCode()) + "[player=" + (player == null ? "null" : player.getName().getString()) + ", isActive=" + (timer > 0) +
				", ticksLeft=" + timer +", entityCount="+ entitiesSpawned.size()+", nextDay="+nextDay + ", day="+day+"]";
	}

	private void logInfo(Object message) {
		HordesLogger.logInfo("["+this+"]" + message);
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
			}
		}
		MinecraftForge.EVENT_BUS.post(event);
	}

	@Override
	public void reset(ServerLevel level) {
		entitiesSpawned.clear();
		HordeSavedData data = HordeSavedData.getData((ServerLevel) level);
		nextDay = data.getNextDay();
		loadedTable = null;
		timer = 0;
	}
}
