package net.smileycorp.hordes.common.hordeevent.capability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.event.HordeEndEvent;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;
import net.smileycorp.hordes.common.event.HordeStartEvent;
import net.smileycorp.hordes.common.event.HordeStartWaveEvent;
import net.smileycorp.hordes.common.hordeevent.HordeEventRegister;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.common.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.common.hordeevent.network.HordeSoundMessage;

class HordeEvent implements IHordeEvent {

	private Set<MobEntity> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private boolean hasChanged = false;
	private Random rand = new Random();

	public HordeEvent(){
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			HordeWorldData data = HordeWorldData.getData(ServerLifecycleHooks.getCurrentServer().overworld());
			nextDay = data.getNextDay();
		}
	}

	@Override
	public void readFromNBT(CompoundNBT nbt) {
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
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt.putInt("timer", timer);
		nbt.putInt("nextDay", nextDay);
		nbt.putInt("day", day);
		hasChanged = false;
		return nbt;
	}

	@Override
	public void update(PlayerEntity player) {
		World world = player.level;
		if (!world.isClientSide && player!=null) {
			if (world.dimension() == World.OVERWORLD) {
				if ((timer % CommonConfigHandler.hordeSpawnInterval.get()) == 0) {
					int amount = (int)(CommonConfigHandler.hordeSpawnAmount.get() * (1+(day/CommonConfigHandler.hordeSpawnDays.get()) * (CommonConfigHandler.hordeSpawnMultiplier.get()-1)));
					List<? extends PlayerEntity>players = world.players();
					for (PlayerEntity entity : players) {
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
	public void spawnWave(PlayerEntity player, int count) {
		cleanSpawns();
		World world = player.level;
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		MinecraftForge.EVENT_BUS.post(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vector3d basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(world, player.blockPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.blockPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
			basepos = DirectionUtils.getClosestLoadedPos(world, player.blockPosition(), basedir, 75, 7, 0);
			i++;
			if (i==20) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basepos = DirectionUtils.getClosestLoadedPos(world, player.blockPosition(), basedir, 75);
				break;
			}
		}
		HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeEventRegister.getSpawnTable(day), this);
		MinecraftForge.EVENT_BUS.post(buildTableEvent);
		WeightedOutputs<HordeSpawnEntry> spawntable = buildTableEvent.spawntable;
		if (spawntable.isEmpty()) {
			logInfo("Spawntable is empty, stopping wave spawn.");
			return;
		}
		if (count > 0) {
			if (player instanceof ServerPlayerEntity) {
				HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new HordeSoundMessage(basedir, startEvent.getSound()), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		} else {
			logInfo("Stopping wave spawn because count is " + count);
		}
		for (int n = 0; n<count; n++) {
			if (entitiesSpawned.size() > CommonConfigHandler.hordeSpawnMax.get()) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vector3d dir = DirectionUtils.getRandomDirectionVecXZ(rand);
			BlockPos pos = DirectionUtils.getClosestLoadedPos(world, basepos, dir, rand.nextInt(10));
			HordeSpawnEntry entry = spawntable.getResult(rand);
			EntityType<?> type = entry.getEntity();
			try {
				MobEntity entity = (MobEntity) type.create(world);
				entity.readAdditionalSaveData(HordeEventRegister.getEntryFor(entity, day).getNBT());
				HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
				MinecraftForge.EVENT_BUS.post(spawnEntityEvent);
				if (!spawnEntityEvent.isCanceled()) {
					entity = spawnEntityEvent.entity;
					entity.readAdditionalSaveData(entry.getNBT());
					pos = spawnEntityEvent.pos;
					entity.finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(pos), null, null, null);
					entity.setPos(pos.getX(), pos.getY(), pos.getZ());
					entity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(100.0D);
					if (!world.addFreshEntity(entity)) Hordes.logError("Unable to spawn entity from " + type, new Exception());
					entity.getCapability(Hordes.HORDESPAWN, null).resolve().get().setPlayerUUID(player.getUUID().toString());
					registerEntity(entity);
					hasChanged = true;
					entity.targetSelector.getRunningGoals().forEach((goal) -> goal.stop());
					if (entity instanceof CreatureEntity) {
						entity.targetSelector.addGoal(1, new HurtByTargetGoal((CreatureEntity) entity));
					}
					entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(entity, PlayerEntity.class, true));
					entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player)); //TODO: debug ai not working correctly on drowned mobs that aren't in water
				} else {
					logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + type);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Hordes.logError("Unable to spawn entity from " + type, e);
			}
		}
	}

	private void cleanSpawns() {
		List<MobEntity> toRemove = new ArrayList<>();
		for (MobEntity entity : entitiesSpawned) {
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
	public boolean isHordeDay(PlayerEntity player) {
		World world = player.level;
		if (world.isClientSide |!(world.dimension() == World.OVERWORLD)) return false;
		return isActive(player) || Math.floor(world.getDayTime()/CommonConfigHandler.dayLength.get())>=nextDay;
	}

	@Override
	public boolean isActive(PlayerEntity player) {
		return timer > 0;
	}

	@Override
	public boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public void setPlayer(PlayerEntity player) {
		Set<MobEntity> toRemove = new HashSet<>();
		for (MobEntity entity : entitiesSpawned) {
			if (entity!=null) {
				GoToEntityPositionGoal task = null;
				for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
					if (entry.getGoal() instanceof GoToEntityPositionGoal) {
						task = (GoToEntityPositionGoal) entry.getGoal();
						break;
					}
				}
				if (task!=null) {
					entity.goalSelector.removeGoal(task);
					entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
				}
			} else toRemove.add(entity);
		}
		entitiesSpawned.removeAll(toRemove);
		hasChanged = true;
	}

	@Override
	public void tryStartEvent(PlayerEntity player, int duration, boolean isCommand) {
		if (player!=null) {
			World world = player.level;
			if (world.dimension() == World.OVERWORLD) {
				HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
				MinecraftForge.EVENT_BUS.post(startEvent);
				if (startEvent.isCanceled()) return;
				HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeEventRegister.getSpawnTable((int) Math.floor(world.getDayTime()/CommonConfigHandler.dayLength.get())), this);
				MinecraftForge.EVENT_BUS.post(buildTableEvent);
				if (!buildTableEvent.spawntable.isEmpty()) {
					timer = duration;
					hasChanged = true;
					sendMessage(player, startEvent.getMessage());
					if (isCommand) day = (int) Math.floor(world.getDayTime()/CommonConfigHandler.dayLength.get());
					else day = nextDay;
				} else {
					logInfo("Spawntable is empty, canceling event start.");
				}
				if (!isCommand) nextDay = HordeWorldData.getData((ServerWorld) world).getNextDay();
			}
		} else Hordes.logError("player is null for " + toString(), new NullPointerException());
	}

	@Override
	public void setNextDay(int day) {
		nextDay=day;
	}

	@Override
	public int getNextDay() {
		return nextDay;
	}

	private void sendMessage(PlayerEntity player, String str) {
		HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(str), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	@Override
	public void stopEvent(PlayerEntity player, boolean isCommand) {
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand);
		MinecraftForge.EVENT_BUS.post(endEvent);
		timer = 0;
		cleanSpawns();
		sendMessage(player, endEvent.getMessage());
		hasChanged = true;
	}

	@Override
	public void removeEntity(MobEntity entity) {
		entitiesSpawned.remove(entity);
	}

	@Override
	public void registerEntity(MobEntity entity) {
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
	}

	public String toString(PlayerEntity player) {
		return "OngoingHordeEvent@" + Integer.toHexString(hashCode()) + "[player=" + (player == null ? "null" : player.getName().getString()) + ", isActive=" + (timer > 0) +
				", ticksLeft=" + timer +", entityCount="+ entitiesSpawned.size()+", nextDay="+nextDay + ", day="+day+"]";
	}

	private void logInfo(Object message) {
		Hordes.logInfo("["+this+"]" + message);
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
				if (entitylist.indexOf(entity) < entitylist.size()-1) builder.append(", ");
			}
			builder.append("}");
			result.add(builder.toString());
		}
		return result;
	}

	@Override
	public void reset(ServerWorld world) {
		entitiesSpawned.clear();
		HordeWorldData data = HordeWorldData.getData((ServerWorld) world);
		nextDay = data.getNextDay();
	}
}
