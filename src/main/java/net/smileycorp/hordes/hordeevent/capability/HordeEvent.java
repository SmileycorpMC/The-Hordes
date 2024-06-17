package net.smileycorp.hordes.hordeevent.capability;

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
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
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
import net.smileycorp.hordes.hordeevent.network.HordeEventPacketHandler;
import net.smileycorp.hordes.hordeevent.network.HordeSoundMessage;
import net.smileycorp.hordes.hordeevent.network.UpdateClientHordeMessage;
import net.smileycorp.hordes.integration.mobspropertiesrandomness.MPRIntegration;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HordeEvent {
	
	private static UUID FOLLOW_RANGE_MODIFIER = UUID.fromString("51cfe045-4248-409e-be37-556d67de4b97");
	private final Random rand;
	private Set<EntityLiving> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay = -1;
	private HordeSpawnData spawnData = null;
	boolean sentDay;
	
	HordeEvent(WorldDataHordes data){
		nextDay = HordeEventConfig.hordeEventByPlayerTime ? HordeEventConfig.spawnFirstDay ? 0 : HordeEventConfig.hordeSpawnDays
				: data.getNextDay();
		rand = data.getRandom();
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		entitiesSpawned.clear();
		if (nbt.hasKey("timer")) timer = nbt.getInteger("timer");
		if (nbt.hasKey("nextDay")) nextDay = nbt.getInteger("nextDay");
		if (nbt.hasKey("day")) day = nbt.getInteger("day");
		if (nbt.hasKey("spawnData")) spawnData = new HordeSpawnData(this, nbt.getCompoundTag("spawnData"));
		if (nbt.hasKey("loadedTable")) {
			spawnData = new HordeSpawnData(this);
			spawnData.setTable(HordeTableLoader.INSTANCE.getTable(new ResourceLocation(nbt.getString("loadedTable"))));
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("timer", timer);
		nbt.setInteger("nextDay", nextDay);
		nbt.setInteger("day", day);
		if (spawnData != null) nbt.setTag("spawnData", spawnData.save());
		return nbt;
	}
	
	public void update(EntityPlayerMP player) {
		World world = player.world;
		if (world.provider.getDimensionType().getId() != 0) return;
		if (spawnData == null) return;
		if (timer % spawnData.getSpawnInterval() == 0) spawnWave(player, getEntityLivingCount(player, world));
		timer--;
		if (timer == 0) stopEvent(player, false);
	}
	
	private int getEntityLivingCount(EntityPlayerMP player, World level) {
		int amount = spawnData.getSpawnAmount();
		List<? extends EntityPlayer> players = level.playerEntities;
		for (EntityPlayer other : players) if (shouldReduce(player, (EntityPlayerMP) other))
			amount = (int) Math.floor(amount * HordeEventConfig.hordeMultiplayerScaling);
		return amount;
	}
	
	private boolean shouldReduce(EntityPlayerMP player, EntityPlayerMP other) {
		if (other == player || player.getDistance(other) > 25) return false;
		HordeEvent horde = WorldDataHordes.getData(other.getServerWorld()).getEvent(other);
		return horde != null && horde.isActive(other);
	}
	
	public void spawnWave(EntityPlayerMP player, int count) {
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
		WorldServer world = player.getServerWorld();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		postEvent(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vec3d basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.getPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
			basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
			if (!spawnData.getSpawnType().canSpawn(world, basepos)) basepos = player.getPosition();
			if (i++ >= HordeEventConfig.hordeSpawnChecks) {
				logInfo("Unable to find unlit pos for horde " + this + " ignoring light level");
				basedir = DirectionUtils.getRandomDirectionVecXZ(rand);
				basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75);
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
			if (entitiesSpawned.size() > HordeEventConfig.hordeSpawnMax) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3d pos = getSpawnPos(world, new Vec3d(basepos));
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
				finalizeEntity(newEntity, player);
			} catch (Exception e) {
				e.printStackTrace();
				logError("Unable to spawn entity from " + type, e);
			}
		}
	}
	
	private Vec3d getSpawnPos(WorldServer level, Vec3d basepos) {
		for (int j = 0; j < 5; j++) {
			double x = basepos.x + rand.nextInt(10);
			double z = basepos.z + rand.nextInt(10);
			Vec3d pos = new Vec3d(x, level.getHeight((int)x, (int)z), z);
			if (spawnData.getSpawnType().canSpawn(level, new BlockPos(pos))) return pos;
		}
		return basepos;
	}
	
	private EntityLiving loadEntity(WorldServer level, EntityPlayerMP player, EntityLiving entity, Vec3d pos, AtomicBoolean cancel) {
		HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
		postEvent(spawnEntityEvent);
		if (!spawnEntityEvent.isCanceled()) {
			entity = spawnEntityEvent.getEntity();
			pos = spawnEntityEvent.getPos();
			entity.onInitialSpawn(level.getDifficultyForLocation(new BlockPos(pos)), null);
			entity.setPosition(pos.x, pos.y, pos.z);
			return entity;
		} else {
			logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + entity.getClass());
			cancel.set(true);
			return entity;
		}
	}
	
	private void finalizeEntity(EntityLiving entity, EntityPlayerMP player) {
		entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER,
				"hordes:horde_range", 75, 0));
		if (Loader.isModLoaded("mpr")) MPRIntegration.addFollowAttribute(entity);
		if (entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) {
			entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID(player.getUniqueID().toString());
			registerEntity(entity, player);
		}
		if (entity instanceof EntityCreature) entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, false));
		entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>((EntityCreature) entity, EntityPlayerMP.class, true));
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof EntityLiving) finalizeEntity((EntityLiving) passenger, player);
	}
	
	private void cleanSpawns() {
		List<EntityLiving> toRemove = new ArrayList<>();
		for (EntityLiving entity : entitiesSpawned) {
			if (entity.isEntityAlive()) continue;
			if (entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) {
				entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID("");
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}
	
	public boolean isHordeDay(EntityPlayerMP player) {
		WorldServer world = player.getServerWorld();
		if (world.provider.getDimensionType().getId() != 0) return false;
		return isActive(player) || getCurrentDay(player) >= nextDay;
	}
	
	public boolean isActive(EntityPlayerMP player) {
		return timer > 0;
	}
	
	public void setPlayer(EntityPlayerMP player) {
		cleanSpawns();
		entitiesSpawned.forEach(entity -> fixGoals(player, entity));
	}
	
	private void fixGoals(EntityPlayerMP player, EntityLiving entity) {
		entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player, spawnData.getEntitySpeed()));
	}
	
	public void tryStartEvent(EntityPlayerMP player, int duration, boolean isCommand) {
		cleanSpawns();
		if (HordeEventConfig.hordesCommandOnly &! isCommand) return;
		if (!isCommand) {
			logInfo("Trying to start horde event on day " + getCurrentDay(player) + " with nextDay " + nextDay + " and time "
					+ player.world.getWorldTime() % HordeEventConfig.dayLength);
		}
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
		if (!isCommand) nextDay = HordeEventConfig.hordeEventByPlayerTime ? nextDay + HordeEventConfig.hordeSpawnDays
				: WorldDataHordes.getData(world).getNextDay();
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
	
	private void sendMessage(EntityPlayerMP player, String str) {
		HordeEventPacketHandler.sendTo(new SimpleStringMessage(str), player);
	}
	
	public void stopEvent(EntityPlayerMP player, boolean isCommand) {
		entitiesSpawned.clear();
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand, spawnData.getEndMessage());
		postEvent(endEvent);
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(nextDay, 0), player);
		timer = 0;
		spawnData = null;
		sendMessage(player, endEvent.getMessage());
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
	}
	
	public void removeEntity(EntityLiving entity) {
		entitiesSpawned.remove(entity);
	}
	
	public void registerEntity(EntityLiving entity, EntityPlayerMP player) {
		if (!isActive(player) || spawnData == null) {
			if (!entity.hasCapability(HordesCapabilities.HORDESPAWN, null)) return;
			entity.getCapability(HordesCapabilities.HORDESPAWN, null).setPlayerUUID("");
			return;
		}
		if (!entitiesSpawned.contains(entity)) entitiesSpawned.add(entity);
		entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player, spawnData.getEntitySpeed()));
	}
	
	private void postEvent(HordePlayerEvent event) {
		HordeScriptLoader.INSTANCE.applyScripts(event);
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	public void reset(WorldServer level) {
		entitiesSpawned.clear();
		WorldDataHordes data = WorldDataHordes.getData(level);
		nextDay = data.getNextDay();
		spawnData = null;
		timer = 0;
	}
	
	public boolean hasSynced() {
		return sentDay;
	}
	
	public void sync(EntityPlayerMP player) {
		HordeEventPacketHandler.sendTo(new UpdateClientHordeMessage(isActive(player) ? day : nextDay, HordeEventConfig.dayLength), player);
		sentDay = true;
	}
	
	public int getDay() {
		return day;
	}
	
	public int getCurrentDay(EntityPlayerMP player) {
		return (int) Math.floor((HordeEventConfig.hordeEventByPlayerTime ? player.getStatFile().readStat(StatList.PLAY_ONE_MINUTE)
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
		return rand;
	}
	
}
