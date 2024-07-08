package net.smileycorp.hordes.hordeevent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

public class HordeEventHandler {
	
	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof EntityLiving &! entity.hasCapability(HordesCapabilities.HORDESPAWN, null))
			event.addCapability(Constants.loc("HordeSpawn"), new HordeSpawn.Provider());
		if (entity instanceof EntityPlayer && entity.world.isRemote &! entity.hasCapability(HordesCapabilities.HORDE_EVENT_CLIENT, null))
			event.addCapability(Constants.loc("HordeEventClient"), new HordeEventClient.Provider());
	}
	
	//update the next day in the horde world data
	@SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || HordeEventConfig.hordesCommandOnly) return;
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer world = server.getWorld(0);
		if (HordeEventConfig.pauseEventServer && world.playerEntities.isEmpty()) return;
		int day = (int) Math.floor(world.getWorldTime() / HordeEventConfig.dayLength);
		WorldDataHordes data = WorldDataHordes.getData(world);
		if (day >= data.getNextDay()) data.setNextDay(world.rand.nextInt(HordeEventConfig.hordeSpawnVariation + 1)
				+ HordeEventConfig.hordeSpawnDays + data.getNextDay());
		data.save();
	}
	
	//spawn the horde at the correct time
	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP) || event.player instanceof FakePlayer) return;
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
		if (HordeEventConfig.pauseEventServer && world.playerEntities.isEmpty()) return;
		HordeEvent horde = WorldDataHordes.getData(world).getEvent(player);
		if (horde == null) return;
		if (!horde.hasSynced()) horde.sync(player);
		if (horde.isActive(player)) {
			horde.update(player);
			return;
		}
		int day = horde.getCurrentDay(player);
		int time = Math.round(world.getWorldTime() % HordeEventConfig.dayLength);
		if (time >= HordeEventConfig.hordeStartTime && time <= HordeEventConfig.hordeStartTime + HordeEventConfig.hordeStartBuffer
				&& day >= horde.getNextDay() && (day > 0 || HordeEventConfig.spawnFirstDay)) {
			horde.tryStartEvent(player, -1, false);
		}
		
	}
	
	//prevent despawning of entities in an active horde
	@SubscribeEvent
	public void tryDespawn(LivingSpawnEvent.AllowDespawn event) {
		EntityPlayerMP player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeEvent horde = WorldDataHordes.getData(player.world).getEvent(player);
		if (horde != null && horde.isActive(player)) event.setResult(Event.Result.DENY);
	}
	
	//sync entity capabilities when added to world
	@SubscribeEvent(priority= EventPriority.LOWEST)
	public void update(LivingEvent.LivingUpdateEvent event) {
		EntityPlayerMP player = HordeSpawn.getHordePlayer(event.getEntity());
		if (player == null) return;
		HordeSpawn cap = event.getEntity().getCapability(HordesCapabilities.HORDESPAWN, null);
		if (cap.isSynced()) return;
		EntityLiving entity = (EntityLiving) event.getEntity();
		for (EntityAITasks.EntityAITaskEntry entry : entity.targetTasks.taskEntries) entry.action.resetTask();
		if (entity instanceof EntityCreature) {
			entity.targetTasks.addTask(1, new EntityAIHurtByTarget((EntityCreature) entity, false));
			entity.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>((EntityCreature) entity, EntityPlayer.class, true));
		}
		HordeEvent horde = WorldDataHordes.getData(player.world).getEvent(player);
		if (horde != null) if (horde.isActive(player)) horde.registerEntity(entity, player);
		cap.setSynced();
	}
	
	//prevent sleeping on horde nights
	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		if (HordeEventConfig.canSleepDuringHorde || !(event.getEntity() instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
		WorldServer world = player.getServerWorld();
		HordeEvent horde = WorldDataHordes.getData(world).getEvent(player);
		if (horde == null) return;
		if (world.isDaytime() |! (world.provider.getDimension() == 0 && (horde.isHordeDay(player) || horde.isActive(player)))) return;
		event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
		player.sendMessage(new TextComponentTranslation(Constants.hordeTrySleep));
	}
	
}
