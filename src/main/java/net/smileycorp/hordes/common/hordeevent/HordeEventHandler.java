package net.smileycorp.hordes.common.hordeevent;

import java.util.UUID;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.smileycorp.atlas.api.entity.ai.EntityAIFindNearestTargetPredicate;
import net.smileycorp.atlas.api.entity.ai.EntityAIGoToEntityPos;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

import com.google.common.base.Predicate;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class HordeEventHandler {
	
	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {
		if (event.phase == Phase.END) {
			World world = event.world;
			if (!world.isRemote) {
				int day = Math.round(world.getWorldTime()/24000);
				int time = Math.round(world.getWorldTime()%24000);
				WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
				if (time == ConfigHandler.hordeStartTime && day % ConfigHandler.hordeSpawnDays == 0 && (day!=0 || ConfigHandler.spawnFirstDay)) {
					for (OngoingHordeEvent hordeEvent: data.getEvents()) {
						if (!hordeEvent.isActive(world)) {
							hordeEvent.tryStartEvent(ConfigHandler.hordeSpawnDuration);
						}
					}
				}
				for (OngoingHordeEvent hordeEvent: data.getEvents()) {
					if (hordeEvent.isActive(world)) {
						hordeEvent.update(world);
						if (hordeEvent.hasChanged()) {
							data.markDirty();
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void tryDespawn(LivingSpawnEvent.AllowDespawn event) {
		World world = event.getWorld();
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.hasCapability(HordeSpawnProvider.HORDESPAWN, null)) {
			IHordeSpawn cap = entity.getCapability(HordeSpawnProvider.HORDESPAWN, null);
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
					OngoingHordeEvent hordeevent = data.getEventForPlayer(uuid);
					if (hordeevent.isActive(world)) {
						event.setResult(Result.DENY);
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onJoin(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		if (!world.isRemote && event.getEntity() instanceof EntityLiving) {
			EntityLiving entity = (EntityLiving) event.getEntity();
			if (entity.hasCapability(HordeSpawnProvider.HORDESPAWN, null)) {
				IHordeSpawn cap = entity.getCapability(HordeSpawnProvider.HORDESPAWN, null);
				if (cap.isHordeSpawned() && DataUtils.isValidUUID(cap.getPlayerUUID())) {
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
					String uuid = cap.getPlayerUUID();
					EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
					WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
					OngoingHordeEvent hordeevent = data.getEventForPlayer(uuid);
					if (player!=null) {
						entity.tasks.addTask(6, new EntityAIGoToEntityPos(entity, player));
					}
					hordeevent.registerEntity(entity);
				}
			}
		}	
	}
	
	@SubscribeEvent
	public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		World world = player.world;
		if (player != null && world != null) {
			if (!world.isRemote) {
				WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
				data.getEventForPlayer(player);
			}
		}
	}
	
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!entity.hasCapability(HordeSpawnProvider.HORDESPAWN, null) && entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
			event.addCapability(ModDefinitions.getResource("HordeSpawn"), new HordeSpawnProvider());
		}
	}
	
	@SubscribeEvent
	public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		IBlockState state = world.getBlockState(event.getPos());
		if (!ConfigHandler.canSleepDuringHorde && state.getBlock() instanceof BlockBed) {
			if (!world.isRemote) {
				WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
				OngoingHordeEvent hordeEvent = data.getEventForPlayer(player);
				if (hordeEvent.isActive(world)) {
					event.setCanceled(true);
					player.sendMessage(new TextComponentTranslation(ModDefinitions.hordeTrySleep));
				}
			}
		}
	}
}
