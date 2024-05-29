package net.smileycorp.hordes.infection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.ai.EntityAINearestAttackableConversion;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.InfectionPacketHandler.InfectMessage;
import net.smileycorp.hordes.infection.capability.IInfection;

import java.util.Random;

@EventBusSubscriber(modid=Constants.MODID)
public class InfectionEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer) || entity instanceof EntityVillager || InfectionRegister.canBeInfected(entity)) {
			event.addCapability(Constants.loc("InfectionCounter"), new IInfection.Provider());
		}
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity != null) {
			if (!entity.world.isRemote) {
				if (InfectionConfig.infectionEntitiesAggroConversions) {
					if (InfectionRegister.canCauseInfection(entity) && entity instanceof EntityCreature) {
						((EntityLiving)entity).targetTasks.addTask(3, new EntityAINearestAttackableConversion((EntityCreature)entity, 10, true, true));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		if (player != null) {
			if (player instanceof EntityPlayerMP) {
				InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(InfectionRegister.getCurePacketData()), (EntityPlayerMP) player);
			}
		}
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		EntityLivingBase entity = event.getEntityLiving();
		ItemStack stack = event.getItem();
		if (entity.isPotionActive(HordesInfection.INFECTED)) {
			if (InfectionRegister.isCure(stack)) {
				entity.removePotionEffect(HordesInfection.INFECTED);
				IInfection cap = entity.getCapability(HordesCapabilities.INFECTION, null);
				if (cap != null) cap.increaseInfection();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		RayTraceResult ray = DirectionUtils.rayTrace(event.getWorld(), event.getEntityPlayer(), 5);
		if (ray != null) {
			if (ray.entityHit instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) ray.entityHit;
				if (entity.isPotionActive(HordesInfection.INFECTED)) {
					if (InfectionRegister.isCure(stack)) {
						entity.removePotionEffect(HordesInfection.INFECTED);
						IInfection cap = entity.getCapability(HordesCapabilities.INFECTION, null);
						if (cap != null) cap.increaseInfection();
						event.setCanceled(true);
						event.setCancellationResult(EnumActionResult.FAIL);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		Entity attacker = event.getSource().getImmediateSource();
		World world = entity.world;
		Random rand = world.rand;
		if (!world.isRemote && InfectionRegister.canCauseInfection(attacker)) {
			if (!entity.isPotionActive(HordesInfection.INFECTED)) {
				if ((entity instanceof EntityPlayer && InfectionConfig.infectPlayers)) {
					int c = rand.nextInt(100);
					if (c <= InfectionConfig.playerInfectChance) {
						entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, InfectionRegister.getInfectionTime(entity), 0));
						InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), (EntityPlayerMP) entity);
					}
				} else if ((entity instanceof EntityVillager && InfectionConfig.infectVillagers)) {
					int c = rand.nextInt(100);
					if (c <= InfectionConfig.villagerInfectChance) {
						entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, InfectionRegister.getInfectionTime(entity), 0));
					}
				} else if (InfectionRegister.canBeInfected(entity)) {
					InfectionRegister.tryToInfect(entity);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		World world = entity.world;
		if (!world.isRemote && (source == HordesInfection.INFECTION_DAMAGE || entity.isPotionActive(HordesInfection.INFECTED))) {
			InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
			MinecraftForge.EVENT_BUS.post(newevent);
			if (newevent.getResult() == Result.DENY) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (entity instanceof EntityVillager) {
			EntityZombieVillager zombie = new EntityZombieVillager(world);
			zombie.setForgeProfession(((EntityVillager) entity).getProfessionForge());
			zombie.setPosition(entity.posX, entity.posY, entity.posZ);
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				zombie.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot));
			}
			zombie.renderYawOffset = entity.renderYawOffset;
			if (entity.hasCustomName()) {
				zombie.setCustomNameTag(entity.getCustomNameTag());
			}
			world.spawnEntity(zombie);
			entity.setDead();
			event.setResult(Result.DENY);
		} else if (InfectionRegister.canBeInfected(entity))  {
			InfectionRegister.convertEntity(entity);
			event.setResult(Result.DENY);
		}
	}

}
