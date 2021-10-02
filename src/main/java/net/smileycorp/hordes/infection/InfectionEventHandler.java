package net.smileycorp.hordes.infection;

import java.util.Random;

import net.minecraft.entity.Entity;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.infection.InfectionPacketHandler.InfectMessage;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class InfectionEventHandler {
	
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
				entity.removeActivePotionEffect(HordesInfection.INFECTED);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = event.getItemStack();
		if (event.getTarget() instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) event.getTarget();
			if (entity.isPotionActive(HordesInfection.INFECTED)) {
				if (InfectionRegister.isCure(stack)) {
					entity.removeActivePotionEffect(HordesInfection.INFECTED);
					if (!player.capabilities.isCreativeMode) {
						ItemStack container = stack.getItem().getContainerItem(stack);
						if (stack.isItemStackDamageable()) {
							stack.damageItem(1, player);
						} else {
							stack.splitStack(1);
						}
						if (stack.isEmpty() && !container.isEmpty()) {
							player.setItemStackToSlot(event.getHand() == EnumHand.OFF_HAND ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND, stack);
						}
					}
					event.setCanceled(true);
					event.setCancellationResult(EnumActionResult.FAIL);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(event.getWorld(), event.getEntityPlayer(), 5);
		if (ray != null) {
			if (ray.entityHit instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) ray.entityHit;
				if (entity.isPotionActive(HordesInfection.INFECTED)) {
					if (InfectionRegister.isCure(stack)) {
						entity.removeActivePotionEffect(HordesInfection.INFECTED);
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
				if ((entity instanceof EntityPlayer && ConfigHandler.infectPlayers)) {
					int c = rand.nextInt(100);
					if (c <= ConfigHandler.playerInfectChance) {
						entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, 0));
						InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), (EntityPlayerMP) entity);
					}
				} else if ((entity instanceof EntityVillager && ConfigHandler.infectVillagers)) {
					int c = rand.nextInt(100);
					if (c <= ConfigHandler.villagerInfectChance) {
						entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, 0));
					}
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
		}
	}
	
	@SubscribeEvent
	public static void onTick(LivingUpdateEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (!world.isRemote && entity.isPotionActive(HordesInfection.INFECTED)) {
			PotionEffect effect = entity.getActivePotionEffect(HordesInfection.INFECTED);
			if (effect.getDuration() < 10000 - ConfigHandler.ticksForEffectStage) {
				int a = effect.getAmplifier();
				if (a < 3) {
					entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, a+1));
				} else {
					entity.attackEntityFrom(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
				}
			}
		}
	}
	
}
