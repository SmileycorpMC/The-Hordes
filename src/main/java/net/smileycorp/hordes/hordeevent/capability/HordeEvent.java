package net.smileycorp.hordes.hordeevent.capability;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.ai.EntityAIHordeTrackPlayer;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.*;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.config.data.hordeevent.HordeScriptLoader;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.Playtime;
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.hordeevent.network.HordeSoundMessage;
import net.smileycorp.hordes.hordeevent.network.UpdateClientHordeMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class HordeEvent {

	private final WorldDataHordes data;
	private static final UUID FOLLOW_RANGE_MODIFIER = UUID.fromString("51cfe045-4248-409e-be37-556d67de4b97");
	private Random rand;
	private final Set<EntityLiving> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay;
	private HordeSpawnData spawnData = null;
	int sentDay = 0;
	private String username;

	HordeEvent(WorldDataHordes data) {
		this.data = data;
		nextDay = HordeEventConfig.spawnFirstDay ? 0 : HordeEventConfig.hordeSpawnDays;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		entitiesSpawned.clear();
		if (nbt.hasKey("timer")) timer = nbt.getInteger("timer");
		if (nbt.hasKey("nextDay")) nextDay = nbt.getInteger("nextDay");
		if (nbt.hasKey("day")) day = nbt.getInteger("day");
		if (nbt.hasKey("spawnData")) spawnData = new HordeSpawnData(this, nbt.getCompoundTag("spawnData"));
		if (!nbt.hasKey("loadedTable")) return;
		spawnData = new HordeSpawnData(this);
		spawnData.setTable(HordeTableLoader.INSTANCE.getTable(new ResourceLocation(nbt.getString("loadedTable"))));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt, UUID uuid) {
		nbt.setInteger("timer", timer);
		nbt.setInteger("nextDay", nextDay);
		nbt.setInteger("day", day);
		if (spawnData != null) nbt.setTag("spawnData", spawnData.save());
		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
		nbt.setString("username", player == null ? username == null ? uuid.toString() : username : player.getGameProfile().getName());
		return nbt;
	}

	public void update(EntityPlayerMP player) {
		if (username == null) username = player.getGameProfile().getName();
		World world = player.world;
		if (world.provider.getDimensionType().getId() != 0) return;
		if (spawnData == null) return;
		if (timer % spawnData.getSpawnInterval() == 0) spawnWave(player, getMobCount(player, world));
		timer--;
		data.setDirty(true);
		if (timer == 0) stopEvent(player, false);
	}

	private int getMobCount(EntityPlayerMP player, World level) {
		int amount = spawnData.getSpawnAmount();
		List<EntityPlayer> players = level.playerEntities;
		for (EntityPlayer other : players) if (shouldReduce(player, (EntityPlayerMP) other))
			amount = (int) Math.floor(amount * HordeEventConfig.hordeMultiplayerScaling);
		return amount;
	}

	private boolean shouldReduce(EntityPlayerMP player, EntityPlayerMP other) {
		if (other == player || player.getDistance(other) > 25) return false;
		HordeEvent horde = data.getEvent(other);
		return horde != null && horde.isActive();
	}

	public void spawnWave(EntityPlayerMP player, int count) {
		Random rand = getRandom();
		cleanSpawns();
		if (spawnData == null) {
			this.rand = null;
			rand = getRandom();
			HordeBuildSpawnDataEvent buildTableEvent = new HordeBuildSpawnDataEvent(player, this);
			if (postEvent(buildTableEvent)) return;
			spawnData = buildTableEvent.getSpawnData();
		}
		if (spawnData == null || spawnData.getTable() == null) {
			logError("Cannot load wave spawn data, cancelling spawns.", new Exception());
			return;
		}
		WorldServer world = player.getServerWorld();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		if (postEvent(startEvent)) return;
		count = startEvent.getCount();
		Vec3d baseDir = DirectionUtils.getRandomDirectionVecXZ(rand);
		BlockPos basePos = getBasePos(world, baseDir, player, true);
		int i = 0;
		while (basePos.equals(player.getPosition())) {
			baseDir = DirectionUtils.getRandomDirectionVecXZ(rand);
			basePos = getBasePos(world, baseDir, player, true);
			if (!spawnData.getSpawnType().canSpawn(world, basePos)) basePos = player.getPosition();
			if (i++ >= HordeEventConfig.hordeSpawnChecks) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				baseDir = DirectionUtils.getRandomDirectionVecXZ(rand);
				basePos = getBasePos(world, baseDir, player, false);
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
		HordeEventPacketHandler.sendTo(new HordeSoundMessage((float) baseDir.x, (float) baseDir.z, spawnData.getSpawnSound()), player);
		for (HordeSpawnEntry entry : spawntable.getResults(rand, count)) {
			if (entitiesSpawned.size() > HordeEventConfig.hordeSpawnMax) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3d pos = getSpawnPos(world, DirectionUtils.centerOf(basePos));
			EntityEntry type = entry.getEntity();
			try {
				AtomicBoolean cancelled = new AtomicBoolean(false);
				NBTTagCompound nbt = entry.getNBT();
				nbt.setString("id", entry.getName().toString());
				EntityLiving newEntity = loadEntity(world, player, (EntityLiving) AnvilChunkLoader.readWorldEntityPos(nbt, world, pos.x, pos.y, pos.z, false), pos, cancelled);
				if (cancelled.get()) continue;
				newEntity.readEntityFromNBT(entry.getNBT());
				if (!(world.spawnEntity(newEntity))) {
					logError("Unable to spawn entity from " + type, new Exception());
					continue;
				}
				finalizeEntity(newEntity, player, true);
			} catch (Exception e) {
				logError("Unable to spawn entity from " + type, e);
			}
		}
	}

	private BlockPos getBasePos(WorldServer world, Vec3d baseDir, EntityPlayerMP player, boolean checkLight) {
		double radius = HordeEventConfig.hordeSpawnDistance;
		BlockPos pos = checkLight ? DirectionUtils.getClosestLoadedPos(world, player.getPosition(), baseDir, radius, 7, 0) :
				DirectionUtils.getClosestLoadedPos(world, player.getPosition(), baseDir, radius);
		HordeFindSpawnPosEvent event = new HordeFindSpawnPosEvent(player, this, baseDir, pos, checkLight);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getPos();
	}

	private Vec3d getSpawnPos(WorldServer world, Vec3d basepos) {
		for (int j = 0; j < 5; j++) {
			double x = basepos.x + rand.nextInt(10);
			double z = basepos.z + rand.nextInt(10);
			Vec3d pos = new Vec3d(x, world.getHeight((int)x, (int)z), z);
			if (spawnData.getSpawnType().canSpawn(world, new BlockPos(pos))) return pos;
		}
		return basepos;
	}

	private EntityLiving loadEntity(WorldServer level, EntityPlayerMP player, EntityLiving entity, Vec3d pos, AtomicBoolean cancel) {
		HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
		if (!postEvent(spawnEntityEvent)) {
			entity = spawnEntityEvent.getEntity();
			pos = spawnEntityEvent.getPos();
			entity.onInitialSpawn(level.getDifficultyForLocation(new BlockPos(pos)), null);
			entity.setPosition(pos.x, pos.y, pos.z);
        } else {
			logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + entity.getClass());
			cancel.set(true);
        }
        return entity;
    }

	private void finalizeEntity(EntityLiving entity, EntityPlayerMP player, boolean addToMobCap) {
		entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
				.applyModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER, "hordes:horde_range", 75, 0));
		if (addToMobCap) registerEntity(entity, player);
		if (entity instanceof EntityCreature) {
			entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, false));
			entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>((EntityCreature) entity, EntityPlayerMP.class, true));
		}
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof EntityLiving) finalizeEntity((EntityLiving) passenger, player, false);
	}

	private void cleanSpawns() {
		List<EntityLiving> toRemove = Lists.newArrayList();
		for (EntityLiving entity : entitiesSpawned) {
			if (entity.isEntityAlive() && entity.isAddedToWorld()) continue;
			toRemove.add(entity);
			if (entity.hasCapability(HordesCapabilities.HORDESPAWN, null))
				entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID("");
		}
		entitiesSpawned.removeAll(toRemove);
	}

	public boolean isHordeDay(EntityPlayerMP player) {
		WorldServer world = player.getServerWorld();
		if (world.provider.getDimensionType().getId() != 0) return false;
		long time = getCurrentTime(player);
		long next = (long) nextDay * (long) HordeEventConfig.dayLength - HordeEventConfig.hordeStartTime;
		return isActive() || (time >= next && time <= next - HordeEventConfig.hordeStartBuffer)
				|| time >= (long) (nextDay + 1) * HordeEventConfig.dayLength;
	}

	public boolean isActive() {
		return timer > 0;
	}

	public void setPlayer(EntityPlayerMP player) {
		setNextDay(player);
		cleanSpawns();
		entitiesSpawned.forEach(entity -> fixGoals(player, entity));
	}

	private void fixGoals(EntityPlayerMP player, EntityLiving entity) {
		Stream<EntityAITasks.EntityAITaskEntry> goals = entity.tasks.taskEntries.stream().filter(entry -> entry.action instanceof EntityAIHordeTrackPlayer);
		goals.forEach(entry -> entity.tasks.removeTask(entry.action));
		entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player, spawnData.getEntitySpeed()));
	}

	public void tryStartEvent(EntityPlayerMP player, int duration, boolean isCommand) {
		rand = data.getRandom(day);
		cleanSpawns();
		if (HordeEventConfig.hordesCommandOnly &! isCommand) return;
		if (!isCommand) logInfo("Trying to start horde event on day " + getCurrentDay(player) + " with nextDay " + nextDay + " and time "
					+ player.world.getWorldTime() % HordeEventConfig.dayLength);
		if (player == null) {
			logError("player is null for " + this, new NullPointerException());
			return;
		}
		WorldServer world = player.getServerWorld();
		if (world.provider.getDimensionType().getId() != 0) return;
		HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
		postEvent(startEvent);
		if (startEvent.isCanceled()) {
			spawnData = null;
			return;
		}
		if (spawnData == null) {
			HordeBuildSpawnDataEvent event = new HordeBuildSpawnDataEvent(player, this);
			if (postEvent(event)) return;
			spawnData = event.getSpawnData();
		}
		data.setDirty(true);
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

	public HordeSpawnTable getSpawnTable() {
		return spawnData == null ? null : spawnData.getTable();
	}

	public HordeSpawnData getSpawnData() {
		return spawnData;
	}

	public void setNextDay(int day) {
		nextDay = day;
	}

	public int getNextDay() {
		return nextDay;
	}

	private void sendMessage(EntityPlayerMP player, String str) {
		HordeEventPacketHandler.sendTo(new SimpleStringMessage(str), player);
	}

	public void stopEvent(EntityPlayerMP player, boolean isCommand) {
		entitiesSpawned.clear();
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand, spawnData.getEndMessage(), spawnData.getCommands());
		postEvent(endEvent);
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(false), player);
		sentDay = getCurrentDay(player);
		timer = 0;
		spawnData = null;
		sendMessage(player, endEvent.getMessage());
		MinecraftServer server = player.getServer();
		for (String command : endEvent.getCommands()) server.getCommandManager().executeCommand(new CommandSenderWrapper(player,
				new Vec3d(player.posX, player.posY, player.posZ), player.getPosition(), 2, player, false), command);
		for (EntityLiving entity : entitiesSpawned) {
			for (EntityAITasks.EntityAITaskEntry entry : entity.tasks.taskEntries) {
				if (!(entry.action instanceof EntityAIHordeTrackPlayer)) continue;
				entity.tasks.removeTask(entry.action);
				break;
			}
			if (!entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) continue;
			entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID("");
			entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(FOLLOW_RANGE_MODIFIER);
		}
		rand = null;
		data.setDirty(true);
	}

	public void removeEntity(EntityLiving entity) {
		entitiesSpawned.remove(entity);
	}

	public void registerEntity(EntityLiving entity, EntityPlayerMP player) {
		if (!isActive() || spawnData == null) {
			if (!entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) return;
			entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID("");
			return;
		}
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
		entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player, spawnData.getEntitySpeed()));
	}

	private boolean postEvent(HordePlayerEvent event) {
		HordeScriptLoader.INSTANCE.applyScripts(event);
		return MinecraftForge.EVENT_BUS.post(event);
	}

	public void reset(EntityPlayerMP player) {
		entitiesSpawned.clear();
		setNextDay(player);
		spawnData = null;
		timer = 0;
		data.setDirty(true);
	}

	private void setNextDay(EntityPlayerMP player) {
		if (!HordeEventConfig.hordeEventByPlayerTime) {
			nextDay = data.getNextDay(day);
			return;
		}
		int currentDay = getCurrentDay(player);
		int expectedDay = HordeEventConfig.spawnFirstDay && currentDay == 0 &! isActive() ? 0 :
				HordeEventConfig.hordeSpawnDays * ((currentDay / HordeEventConfig.hordeSpawnDays) + 1);
		if (nextDay <= getCurrentDay(player) || Math.abs(nextDay - expectedDay) > HordeEventConfig.hordeSpawnDays + HordeEventConfig.hordeSpawnVariation) {
			if (HordeEventConfig.hordeSpawnVariation > 0) {
				expectedDay += getRandom().nextInt(HordeEventConfig.hordeSpawnVariation);
				rand = null;
			}
			nextDay = expectedDay;
		}
	}

	public boolean hasSynced(int day) {
		return sentDay >= day;
	}

	public void sync(EntityPlayerMP player, int day) {
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(isHordeDay(player)), player);
		sentDay = day;
	}

	public int getDay() {
		return day;
	}

	public long getCurrentTime(EntityPlayerMP player) {
		return HordeEventConfig.hordeEventByPlayerTime ? ((Playtime)player).getPlaytime() : player.world.getWorldTime();
	}

	public int getCurrentDay(EntityPlayerMP player) {
		return (int) Math.floor((HordeEventConfig.hordeEventByPlayerTime ? ((Playtime)player).getPlaytime()
				: player.world.getWorldTime()) / HordeEventConfig.dayLength);
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
		List<EntityLiving> entitylist = new ArrayList<>(entitiesSpawned);
		for (int i = 0; i < entitylist.size(); i += 10) {
			List<EntityLiving> sublist = entitylist.subList(i, Math.min(i+9, entitylist.size()-1));
			StringBuilder builder = new StringBuilder();
			builder.append("		");
			for (EntityLiving entity : sublist) {
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
		if (rand == null) rand = data.getRandom(day);
		return rand;
	}

}
