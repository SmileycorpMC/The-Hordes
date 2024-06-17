package net.smileycorp.hordes.infection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.ai.EntityAINearestAttackableConversion;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

@EventBusSubscriber(modid=Constants.MODID)
public class InfectionEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer) || entity instanceof EntityVillager || InfectionDataLoader.INSTANCE.canBeInfected(entity)) {
			event.addCapability(Constants.loc("InfectionCounter"), new Infection.Provider());
		}
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity == null) return;
		if (entity.world.isRemote |! InfectionConfig.infectionEntitiesAggroConversions |! (entity instanceof EntityCreature)) return;
		if (InfectionDataLoader.INSTANCE.canCauseInfection(entity))
			((EntityLiving)entity).targetTasks.addTask(3, new EntityAINearestAttackableConversion((EntityCreature)entity));
	}
	
	@SubscribeEvent
	public void logIn(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP)
			InfectionDataLoader.INSTANCE.syncData((EntityPlayerMP) event.player);
	}
	
	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		EntityLivingBase entity = event.getEntityLiving();
		ItemStack stack = event.getItem();
		if (InfectionDataLoader.INSTANCE.applyImmunity(entity, stack)) return;
		if (!(entity.isPotionActive(HordesInfection.INFECTED) && InfectionDataLoader.INSTANCE.isCure(stack))) return;
		if (entity.hasCapability(HordesCapabilities.INFECTION, null))
			entity.getCapability(HordesCapabilities.INFECTION, null).increaseInfection();
		entity.removePotionEffect(HordesInfection.INFECTED);
		if (entity.world.isRemote) return;
		InfectionPacketHandler.send(entity, new CureEntityMessage(entity));
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(event.getWorld(), event.getEntityPlayer(), 5);
		if (!(ray.typeOfHit == RayTraceResult.Type.ENTITY)) return;
		if (!(ray.entityHit instanceof EntityLivingBase)) return;
		EntityLivingBase entity = (EntityLivingBase) ray.entityHit;
		if (entity instanceof EntityPlayer |!(entity.isPotionActive(HordesInfection.INFECTED)
				|| InfectionDataLoader.INSTANCE.isCure(stack))) return;
		entity.removePotionEffect(HordesInfection.INFECTED);
		if (entity.hasCapability(HordesCapabilities.INFECTION, null))
			entity.getCapability(HordesCapabilities.INFECTION, null).increaseInfection();
		event.setCanceled(true);
		event.setCancellationResult(EnumActionResult.FAIL);
	}
	
	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		Entity attacker = event.getSource().getImmediateSource();
		World world = entity.world;
		if (world.isRemote | !(attacker instanceof EntityLiving)) return;
		if (!InfectionDataLoader.INSTANCE.canCauseInfection(attacker) || entity.isPotionActive(HordesInfection.INFECTED)) return;
		if (InfectionDataLoader.INSTANCE.canBeInfected(entity))
			InfectionDataLoader.INSTANCE.tryToInfect(entity, (EntityLiving) attacker, event.getSource(), event.getAmount());
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		World world = entity.world;
		if (world.isRemote || !(source == HordesInfection.INFECTION_DAMAGE || entity.isPotionActive(HordesInfection.INFECTED))) return;
		InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
		MinecraftForge.EVENT_BUS.post(newevent);
		if (newevent.getResult() == Result.DENY) {
			event.setCanceled(true);
			if (!(entity instanceof EntityTameable)) return;
			EntityLivingBase owner = ((EntityTameable) entity).getOwner();
			if (!(owner instanceof EntityPlayerMP)) return;
			owner.sendMessage(new TextComponentTranslation("death.attack.infection.zombified", entity.getDisplayName()));
		}
	}
	
	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (entity instanceof EntityPlayer) return;
		if (entity instanceof EntityVillager && world instanceof WorldServer) {
			EntityVillager villager = (EntityVillager) entity;
			EntityZombieVillager zombie = new EntityZombieVillager(world);
			if (zombie != null) {
				zombie.onInitialSpawn(world.getDifficultyForLocation(zombie.getPosition()), null);
				zombie.setForgeProfession(villager.getProfessionForge());
				zombie.setChild(villager.isChild());
			}
			event.setResult(Result.DENY);
		} else if (InfectionDataLoader.INSTANCE.canBeInfected(entity))  {
			if (InfectionDataLoader.INSTANCE.convertEntity(entity)) return;
			event.setResult(Result.DENY);
		}
	}
	
	@SubscribeEvent
	public void canApplyEffect(PotionEvent.PotionApplicableEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.world.isRemote) return;
		if (event.getPotionEffect().getPotion() == HordesInfection.INFECTED && PotionInfected.preventInfection(entity)) {
			event.setResult(Result.DENY);
			if (entity instanceof EntityPlayerMP) InfectionPacketHandler.sendTo(new InfectMessage(true), (EntityPlayerMP) entity);
		}
	}
	
	@SubscribeEvent
	public void applyEffect(PotionEvent.PotionAddedEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.world.isRemote) return;
		if (event.getPotionEffect().getPotion() == HordesInfection.IMMUNITY && entity.isPotionActive(HordesInfection.INFECTED)) {
			entity.removePotionEffect(HordesInfection.INFECTED);
			InfectionPacketHandler.send(entity, new CureEntityMessage(entity));
		}
	}

}
