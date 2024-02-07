package net.smileycorp.hordes.hordeevent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.atlas.api.entity.ai.EntityAIFindNearestTargetPredicate;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ai.EntityAIHordeTrackPlayer;
import net.smileycorp.hordes.common.event.*;
import net.smileycorp.hordes.integration.mobspropertiesrandomness.MPRIntegration;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OngoingHordeEvent implements IOngoingHordeEvent {

	private Set<EntityLiving> entitiesSpawned = new HashSet<>();
	private int timer = 0;
	private int day = 0;
	private int nextDay;
	private final World world;
	private EntityPlayer player;
	private boolean hasChanged = false;

	public OngoingHordeEvent(World world, EntityPlayer player) {
		this.world=world;
		this.player=player;
		if (world!=null) {
			WorldDataHordeEvent data = WorldDataHordeEvent.getData(world);
			nextDay = data.getNextDay();
			if (data.hasLegacyData(player.getUniqueID())) {
				readFromNBT(data.getLegacyData(player.getUniqueID()));
			}
		}
	}

	public OngoingHordeEvent() {
		world = null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		entitiesSpawned.clear();
		if (nbt.hasKey("timer")) {
			timer = nbt.getInteger("timer");
		}
		if (nbt.hasKey("nextDay")) {
			nextDay = nbt.getInteger("nextDay");
		}
		if (nbt.hasKey("day")) {
			day = nbt.getInteger("day");
		}
		if (nbt.hasKey("entities")) {
			if (world!=null) {
				for (int id : nbt.getIntArray("entities")) {
					Entity entity = world.getEntityByID(id);
					if (entity instanceof EntityLiving) entitiesSpawned.add((EntityLiving) entity);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("timer", timer);
		nbt.setInteger("nextDay", nextDay);
		nbt.setInteger("day", day);
		int[] entities = new int[]{};
		for (EntityLiving entity : entitiesSpawned) {
			if (entity!=null) if (entity.isAddedToWorld() &! entity.isDead) ArrayUtils.add(entities, entity.getEntityId());
		}
		nbt.setIntArray("entities", entities);
		hasChanged = false;
		return nbt;
	}

	@Override
	public void update(World world) {
		if (!world.isRemote && player!=null) {
			if (player.world.provider.getDimension()==0) {
				if ((timer % ConfigHandler.hordeSpawnInterval) == 0) {
					int amount = (int)(ConfigHandler.hordeSpawnAmount * (1+(day/ConfigHandler.hordeSpawnDays) * (ConfigHandler.hordeSpawnMultiplier-1)));
					List<EntityPlayer>players = world.getEntities(EntityPlayer.class, p -> p != player);
					for (EntityPlayer entity : players) {
						if (player.getDistance(entity)<=25) {
							amount = (int) Math.floor(amount * ConfigHandler.hordeMultiplayerScaling);
						}
					}
					spawnWave(world, amount);
				}
				timer--;
				if (timer == 0) {
					stopEvent(world, false);
				}
				hasChanged = true;
			}
		}
	}

	@Override
	public void spawnWave(World world, int count) {
		cleanSpawns();
		HordeStartWaveEvent startEvent = new HordeStartWaveEvent(player, this, count);
		MinecraftForge.EVENT_BUS.post(startEvent);
		if (startEvent.isCanceled()) return;
		count = startEvent.getCount();
		Vec3d basedir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.getPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
			basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
			i++;
			if (i==20) {
				logInfo("Unable to find unlight pos ");
				basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75);
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
			if (player instanceof EntityPlayerMP) {
				HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new HordeSoundMessage(basedir, startEvent.getSound()), (EntityPlayerMP) player);
			}
		} else {
			logInfo("Stopping wave spawn because count is " + count);
		}
		for (int n = 0; n<count; n++) {
			if (entitiesSpawned.size() > ConfigHandler.hordeSpawnMax) {
				logInfo("Can't spawn wave because max cap has been reached");
				return;
			}
			Vec3d dir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
			BlockPos pos = DirectionUtils.getClosestLoadedPos(world, basepos, dir, world.rand.nextInt(10));
			HordeSpawnEntry entry = spawntable.getResult(world.rand);
			Class<? extends EntityLiving> clazz = entry.getEntity();
			try {
				EntityLiving entity = clazz.getConstructor(World.class).newInstance(world);
				HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos, this);
				MinecraftForge.EVENT_BUS.post(spawnEntityEvent);
				if (!spawnEntityEvent.isCanceled()) {
					entity = spawnEntityEvent.entity;
					pos = spawnEntityEvent.pos;
					entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
					entity.readFromNBT(entry.getTagCompound());
					entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
					if (!world.spawnEntity(entity)) Hordes.logError("Unable to spawn entity from " + clazz, new Exception());
					else {
						finalizeEntity(entity, world, player);
					}
				} else {
					logInfo("Entity spawn event has been cancelled, not spawning entity  of class " + clazz);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Hordes.logError("Unable to spawn entity from " + clazz, e);
			}
		}
	}

	private void finalizeEntity(EntityLiving entity, World world2, EntityPlayer player2) {
		entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
		if (Loader.isModLoaded("mpr")) MPRIntegration.addFollowAttribute(entity);
		entity.getCapability(Hordes.HORDESPAWN, null).setPlayerUUID(player.getUniqueID().toString());
		entity.enablePersistence();
		registerEntity(entity);
		hasChanged = true;
		entity.targetTasks.taskEntries.clear();
		if (entity instanceof EntityCreature) {
			entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, true));
			entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>((EntityCreature) entity, EntityPlayer.class, false));
		} else {
			entity.targetTasks.addTask(1, new EntityAIFindNearestTargetPredicate(entity, e -> e instanceof EntityPlayer));
		}
		entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player));
		for (Entity passenger : entity.getPassengers()) if (passenger instanceof EntityLiving) finalizeEntity((EntityLiving) passenger, world, player);
	}

	private void cleanSpawns() {
		List<EntityLiving> toRemove = new ArrayList<>();
		for (EntityLiving entity : entitiesSpawned) {
			if (entity != null) {
				if (entity.isDead) {
					if (entity.hasCapability(Hordes.HORDESPAWN, null)) {
						IHordeSpawn cap = entity.getCapability(Hordes.HORDESPAWN, null);
						cap.setPlayerUUID("");
						toRemove.add(entity);
					}
				}
			} else {
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
	}

	@Override
	public boolean isHordeDay(World world) {
		if (world.isRemote || player==null) return false;
		if (world!=this.world) return false;
		return isActive(world) || Math.floor(world.getWorldTime()/ConfigHandler.dayLength)>=nextDay;
	}

	@Override
	public boolean isActive(World world) {
		return timer > 0;
	}

	@Override
	public boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public EntityPlayer getPlayer() {
		return player;
	}

	@Override
	public void setPlayer(EntityPlayer player) {
		this.player = player;
		Set<EntityLiving> toRemove = new HashSet<>();
		for (EntityLiving entity : entitiesSpawned) {
			if (entity != null) {
				EntityAIHordeTrackPlayer task = null;
				for (EntityAITaskEntry entry : entity.tasks.taskEntries) {
					if (entry.action instanceof EntityAIHordeTrackPlayer) {
						task = (EntityAIHordeTrackPlayer) entry.action;
						break;
					}
				}
				if (task != null) {
					entity.tasks.removeTask(task);
					entity.tasks.addTask(6, new EntityAIHordeTrackPlayer(entity, player));
				}
			} else toRemove.add(entity);
		}
		entitiesSpawned.removeAll(toRemove);
		hasChanged = true;
	}

	@Override
	public void tryStartEvent(int duration, boolean isCommand) {
		if (player!=null) {
			if (player.world.provider.getDimension()==0) {
				HordeStartEvent startEvent = new HordeStartEvent(player, this, isCommand);
				MinecraftForge.EVENT_BUS.post(startEvent);
				if (startEvent.isCanceled()) return;
				HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeEventRegister.getSpawnTable((int) Math.floor(world.getWorldTime()/ConfigHandler.dayLength)), this);
				MinecraftForge.EVENT_BUS.post(buildTableEvent);
				WeightedOutputs<HordeSpawnEntry> spawntable = buildTableEvent.spawntable;
				if (!spawntable.isEmpty()) {
					timer = duration;
					hasChanged = true;
					sendMessage(startEvent.getMessage());
					if (isCommand) day = (int) Math.floor(world.getWorldTime()/ConfigHandler.dayLength);
					else day = nextDay;
				} else {
					logInfo("Spawntable is empty, canceling event start.");
				}
				if (!isCommand) nextDay = WorldDataHordeEvent.getData(world).getNextDay();
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

	private void sendMessage(String str) {
		HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(str), (EntityPlayerMP) player);
	}

	@Override
	public void stopEvent(World world, boolean isCommand) {
		HordeEndEvent endEvent = new HordeEndEvent(player, this, isCommand);
		MinecraftForge.EVENT_BUS.post(endEvent);
		timer = 0;
		cleanSpawns();
		sendMessage(endEvent.getMessage());
		List<EntityLiving> toRemove = new ArrayList<>();
		for (EntityLiving entity : entitiesSpawned) {
			if (entity.hasCapability(Hordes.HORDESPAWN, null)) {
				IHordeSpawn cap = entity.getCapability(Hordes.HORDESPAWN, null);
				cap.setPlayerUUID("");
				toRemove.add(entity);
			}
		}
		entitiesSpawned.removeAll(toRemove);
		hasChanged = true;
	}

	@Override
	public void removeEntity(EntityLiving entity) {
		entitiesSpawned.remove(entity);
	}

	@Override
	public void registerEntity(EntityLiving entity) {
		if (!entitiesSpawned.contains(entity) && entity != null) {
			if (entity.isAddedToWorld() &! entity.isDead) entitiesSpawned.add(entity);
		}
	}

	@Override
	public String toString() {
		return "OngoingHordeEvent@" + Integer.toHexString(hashCode()) + "[player=" + (player == null ? "null" : player.getName()) + ", isActive=" + (timer > 0) +
				", ticksLeft=" + timer +", entityCount="+ entitiesSpawned.size()+", nextDay="+nextDay + ", day="+day+"]";
	}

	private void logInfo(Object message) {
		Hordes.logInfo("["+this+"]" + message);
	}

	public List<String> getEntityStrings() {
		List<String> result = new ArrayList<>();
		result.add("	entities: {");
		List<EntityLiving> entitylist = new ArrayList<>(entitiesSpawned);
		for (int i = 0; i < entitylist.size(); i += 10) {
			List<EntityLiving> sublist = entitylist.subList(i, Math.min(i+9, entitylist.size()-1));
			StringBuilder builder = new StringBuilder();
			builder.append("		");
			for (EntityLiving entity : sublist) {
				builder.append(entity.getClass().getSimpleName() + "@");
				builder.append(Integer.toHexString(entity.hashCode()));
				if (entitylist.indexOf(entity) < entitylist.size()-1) builder.append(", ");
			}
			builder.append("}");
			result.add(builder.toString());
		}
		return result;
	}
}
