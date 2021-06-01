package net.smileycorp.hordes.common.hordeevent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.smileycorp.atlas.api.IOngoingEvent;
import net.smileycorp.atlas.api.entity.ai.EntityAIFindNearestTargetPredicate;
import net.smileycorp.atlas.api.entity.ai.EntityAIGoToPos;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.TheHordes;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.event.HordeSpawnEntityEvent;

import com.google.common.base.Predicate;

public class OngoingHordeEvent implements IOngoingEvent {

	private Set<WeakReference<EntityLiving>> entitiesSpawned = new HashSet<WeakReference<EntityLiving>>();
	private int timer = 0;
	private final EntityPlayer player;
	private boolean hasChanged = false;
	
	public OngoingHordeEvent(EntityPlayer player) {
		this.player=player;
		
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("timer")) {
			timer = nbt.getInteger("timer");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("timer", timer);
		hasChanged = false;
		return nbt;
	}
	
	@Override
	public void update(World world) {
		if (!world.isRemote && player!=null) {
			int day = Math.round(world.getWorldTime()/24000);
			if (isActive(world)) {
				if ((timer % ConfigHandler.hordeSpawnInterval) == 0) {
					int amount = (int)(ConfigHandler.hordeSpawnAmount * (1+(day/ConfigHandler.hordeSpawnDays) * (1-ConfigHandler.hordeSpawnMultiplier)));
					spawnWave(world, amount);
				}
				timer--;
				if (timer == 0) {
					ITextComponent message = new TextComponentTranslation(ModDefinitions.hordeEventEnd);
					message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
					player.sendMessage(message);
				}
				hasChanged = true;
			}
		}
	}
	
	public void spawnWave(World world, int count) {
		cleanSpawns();
		int day = Math.round(world.getWorldTime()/24000);
		Vec3d basedir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
		BlockPos basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
		int i = 0;
		while (basepos.equals(player.getPosition())) {
			basedir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
			basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75, 7, 0);
			i++;
			if (i==20) {
				TheHordes.logInfo("Unable to find unlight pos ");
				basepos = DirectionUtils.getClosestLoadedPos(world, player.getPosition(), basedir, 75);
				break;
			}
		}
		HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeEventRegister.getSpawnTable(day), basepos);
		MinecraftForge.EVENT_BUS.post(buildTableEvent);
		WeightedOutputs<Class<? extends EntityLiving>> spawntable = buildTableEvent.spawntable;
		if (spawntable.isEmpty()) {
			TheHordes.logError("Spawntable is empty, stopping wave spawn.", new NullPointerException());
			return;
		}
		if (count > 0 && player instanceof EntityPlayerMP) {
			HordeEventPacketHandler.NETWORK_INSTANCE.sendTo(new HordeSoundMessage(), (EntityPlayerMP) player);
		}
		for (int n = 0; n<count; n++) {
			if (entitiesSpawned.size()  > ConfigHandler.hordeSpawnMax) {
				return;
			}
			Vec3d dir = DirectionUtils.getRandomDirectionVecXZ(world.rand);
			BlockPos pos = DirectionUtils.getClosestLoadedPos(world, basepos, dir, world.rand.nextInt(10));
			Class<? extends EntityLiving> clazz = spawntable.getResult(world.rand);
			try {
				EntityLiving entity = clazz.getConstructor(World.class).newInstance(world);
				HordeSpawnEntityEvent spawnEntityEvent = new HordeSpawnEntityEvent(player, entity, pos);
				MinecraftForge.EVENT_BUS.post(spawnEntityEvent);
				if (!spawnEntityEvent.isCanceled()) {
					entity = spawnEntityEvent.entity;
					pos = spawnEntityEvent.pos;
					entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
					entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
					entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
					world.spawnEntity(entity);
					entity.getCapability(HordeSpawnProvider.HORDESPAWN, null).setPlayerUUID(player.getUniqueID().toString());
					registerEntity(entity);
					hasChanged = true;
					entity.targetTasks.taskEntries.clear();
					if (entity instanceof EntityCreature) {
						entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, true));
						entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget((EntityCreature) entity, EntityPlayer.class, false));
					} else {
						entity.targetTasks.addTask(1, new EntityAIFindNearestTargetPredicate(entity, new Predicate<EntityLivingBase>(){
							@Override
							public boolean apply(EntityLivingBase entity) {
								return entity instanceof EntityPlayer;
							}}));
					}
					entity.tasks.addTask(6, new EntityAIGoToPos(entity, player.getPosition()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				TheHordes.logError("Unable to spawn entity from " + clazz, e);
			}
		}
	}

	private void cleanSpawns() {
		List<WeakReference<EntityLiving>> toRemove = new ArrayList<WeakReference<EntityLiving>>();
		for (WeakReference<EntityLiving> ref : entitiesSpawned) {
			if (ref != null && ref.get() != null) {
				EntityLiving entity = ref.get();
				if (entity.isDead) {
					if (entity.hasCapability(HordeSpawnProvider.HORDESPAWN, null)) {
						IHordeSpawn cap = entity.getCapability(HordeSpawnProvider.HORDESPAWN, null);
						cap.setPlayerUUID("");
						toRemove.add(ref);
					}
				}
			} else toRemove.add(ref);
		}
		entitiesSpawned.removeAll(toRemove);
	}

	@Override
	public boolean isActive(World world) {
		return timer > 0;
	}
	
	public boolean hasChanged() {
		return hasChanged;
	}
	
	public EntityPlayer getPlayer() {
		return player;
	}

	public void tryStartEvent(int duration) {
		if (player!=null) {
			int day = Math.round(player.world.getWorldTime()/24000);
			HordeBuildSpawntableEvent buildTableEvent = new HordeBuildSpawntableEvent(player, HordeEventRegister.getSpawnTable(day), player.getPosition());
			MinecraftForge.EVENT_BUS.post(buildTableEvent);
			WeightedOutputs<Class<? extends EntityLiving>> spawntable = buildTableEvent.spawntable;
			if (!spawntable.isEmpty()) {
				timer = duration;
				hasChanged = true;
				ITextComponent message = new TextComponentTranslation(ModDefinitions.hordeEventStart);
				message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
				player.sendMessage(message);
			} else {
				TheHordes.logError("Spawntable is empty, canceling event start.", new NullPointerException());
			}
		}
	}

	public void stopEvent() {
		timer = 0;
		hasChanged = true;
		cleanSpawns();
		ITextComponent message = new TextComponentTranslation(ModDefinitions.hordeEventEnd);
		message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
		player.sendMessage(message);
	}

	public void removeEntity(EntityLiving entity) {
		entitiesSpawned.remove(entity);
	}

	public void registerEntity(EntityLiving entity) {
		if (!entitiesSpawned.contains(entity)) {
			entitiesSpawned.add(new WeakReference(entity));
		}
	}
	
}
