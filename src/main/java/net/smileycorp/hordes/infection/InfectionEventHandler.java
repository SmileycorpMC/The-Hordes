package net.smileycorp.hordes.infection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
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
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.infection.InfectionData;
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
		if (!(entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) &! InfectionData.INSTANCE.canBeInfected(entity)) return;
		event.addCapability(Constants.loc("InfectionCounter"), new Infection.Provider());
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof EntityLivingBase)) return;
		EntityLivingBase entity = (EntityLivingBase) event.getEntity();
		if (entity.world.isRemote) return;
		if (InfectionData.INSTANCE.canBeInfected(entity)) entity.getEntityAttribute(HordesInfection.INFECTION_RESISTANCE)
				.setBaseValue(InfectionData.INSTANCE.getProtection(entity.getClass()));
		if (!InfectionData.INSTANCE.hasInfectAttribute(entity)) return;
		entity.getEntityAttribute(HordesInfection.INFECTIVITY).setBaseValue(InfectionData.INSTANCE.getInfectionChance(entity.getClass()));
		if (!InfectionConfig.infectionEntitiesAggroConversions |! (entity instanceof EntityCreature)) return;
		if (InfectionData.INSTANCE.canCauseInfection(entity))
			((EntityLiving)entity).targetTasks.addTask(3, new EntityAINearestAttackableTarget<>((EntityCreature)entity,
					EntityLivingBase.class, 10, true, false, InfectionData.INSTANCE::infectedTarget));
	}
	
	@SubscribeEvent
	public void logIn(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP)
			InfectionData.INSTANCE.syncData((EntityPlayerMP) event.player);
	}
	
	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		EntityLivingBase entity = event.getEntityLiving();
		ItemStack stack = event.getItem();
		if (InfectionData.INSTANCE.applyImmunity(entity, stack)) return;
		if (!(entity.isPotionActive(HordesInfection.INFECTED) && InfectionData.INSTANCE.isCure(stack))) return;
		if (entity.hasCapability(HordesCapabilities.INFECTION, null))
			entity.getCapability(HordesCapabilities.INFECTION, null).increaseInfection();
		entity.removePotionEffect(HordesInfection.INFECTED);
		if (entity.world.isRemote) return;
		InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(event.getWorld(), event.getEntityPlayer(), 5);
		if (!(ray.typeOfHit == RayTraceResult.Type.ENTITY)) return;
		if (!(ray.entityHit instanceof EntityLivingBase)) return;
		EntityLivingBase entity = (EntityLivingBase) ray.entityHit;
		if (entity instanceof EntityPlayer |!(entity.isPotionActive(HordesInfection.INFECTED)
				|| InfectionData.INSTANCE.isCure(stack))) return;
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
		if (world.isRemote |! (attacker instanceof EntityLiving)) return;
		if (!InfectionData.INSTANCE.canCauseInfection(attacker) || entity.isPotionActive(HordesInfection.INFECTED)) return;
		if (InfectionData.INSTANCE.canBeInfected(entity))
			InfectionData.INSTANCE.tryToInfect(entity, (EntityLiving) attacker, event.getSource(), event.getAmount());
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		World world = entity.world;
		if (world.isRemote || !(source == HordesInfection.INFECTION_DAMAGE || entity.isPotionActive(HordesInfection.INFECTED))) return;
		if (MinecraftForge.EVENT_BUS.post(new InfectionDeathEvent(entity, event.getSource()))) {
			event.setCanceled(true);
			return;
		}
		if (!(entity instanceof EntityTameable)) return;
		EntityLivingBase owner = ((EntityTameable) entity).getOwner();
		if (!(owner instanceof EntityPlayerMP)) return;
		owner.sendMessage(new TextComponentTranslation("death.attack.infection.zombified", entity.getDisplayName()));
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity instanceof EntityPlayer) return;
		if (!InfectionData.INSTANCE.canBeInfected(entity)) return;
		InfectionData.INSTANCE.convertEntity((EntityLiving)entity);
	}
	
	@SubscribeEvent
	public void canApplyEffect(PotionEvent.PotionApplicableEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.world.isRemote) return;
		if (PotionInfected.preventInfection(entity)) {
			event.setResult(Result.DENY);
			if (entity instanceof EntityPlayerMP)
				InfectionPacketHandler.sendTo(new InfectMessage(true), (EntityPlayerMP) entity);
		} else if (entity.isPotionActive(HordesInfection.INFECTED) && entity.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier()
				< event.getPotionEffect().getAmplifier()) entity.removePotionEffect(HordesInfection.INFECTED);
	}
	
	@SubscribeEvent
	public void applyEffect(PotionEvent.PotionAddedEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity.world.isRemote) return;
		if (event.getPotionEffect().getPotion() == HordesInfection.IMMUNITY && entity.isPotionActive(HordesInfection.INFECTED)) {
			entity.removePotionEffect(HordesInfection.INFECTED);
			InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
		}
	}

	@SubscribeEvent
	public void effectExpired(PotionEvent.PotionExpiryEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		PotionEffect instance = event.getPotionEffect();
		if (instance == null) return;
		if (instance.getPotion() == HordesInfection.INFECTED && InfectionConfig.enableMobInfection) {
			int amplifier = instance.getAmplifier();
			if (amplifier < 3) {
				entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, PotionInfected.getInfectionTime(entity), amplifier + 1));
				if (entity instanceof EntityPlayerMP) InfectionPacketHandler.sendTo(new InfectMessage(false), (EntityPlayerMP) entity);
			}
			else entity.attackEntityFrom(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
		}
	}

	@SubscribeEvent
	public void addEntityAttributes(EntityEvent.EntityConstructing event) {
		Entity entity = event.getEntity();
		if (!(event.getEntity() instanceof EntityLivingBase)) return;
		AbstractAttributeMap map = ((EntityLivingBase) entity).getAttributeMap();
		map.registerAttribute(HordesInfection.INFECTION_RESISTANCE);
		map.registerAttribute(HordesInfection.INFECTIVITY);
	}

}
