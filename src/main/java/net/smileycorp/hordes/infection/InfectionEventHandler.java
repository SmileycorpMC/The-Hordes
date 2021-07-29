package net.smileycorp.hordes.infection;

import java.awt.Color;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.infection.entities.EntityZombiePlayer;

import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class InfectionEventHandler {
	

	@SubscribeEvent
	public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
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
			if ((entity instanceof EntityPlayer && ConfigHandler.infectPlayers)) {
				int c = rand.nextInt(100);
				if (c <= ConfigHandler.playerInfectChance) {
					entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, 0));
				}
			} else if ((entity instanceof EntityVillager && ConfigHandler.infectVillagers)) {
				int c = rand.nextInt(100);
				if (c <= ConfigHandler.villagerInfectChance) {
					entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, 0));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (!world.isRemote && (event.getSource() == HordesInfection.INFECTION_DAMAGE || entity.isPotionActive(HordesInfection.INFECTED))) {
			InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
			MinecraftForge.EVENT_BUS.post(newevent);
			if (newevent.getResult() == Result.DENY) {
				event.setCanceled(true);
			}
		}	
	}
	
	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (entity instanceof EntityPlayer) {
			EntityZombiePlayer zombie = new EntityZombiePlayer((EntityPlayer)entity);
			zombie.setPosition(entity.posX, entity.posY, entity.posZ);
			zombie.renderYawOffset = entity.renderYawOffset;
			world.spawnEntity(zombie);
			//sendDeathMessage(entity);
			event.setResult(Result.DENY);
		} else if (entity instanceof EntityVillager) {
			EntityZombieVillager zombie = new EntityZombieVillager(world);
			zombie.setForgeProfession(((EntityVillager) entity).getProfessionForge());
			zombie.setPosition(entity.posX, entity.posY, entity.posZ);
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				zombie.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot));
			}
			zombie.renderYawOffset = entity.renderYawOffset;
			if (entity.hasCustomName()) {
				zombie.setCustomNameTag(entity.getCustomNameTag());
				//sendDeathMessage(entity);
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
					entity.removePotionEffect(HordesInfection.INFECTED);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Pre event){
		if (ConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player!= null && event.getType() == ElementType.POTION_ICONS) {
				if (player.isPotionActive(HordesInfection.INFECTED)) {
					int level = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
			    	Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.05f*level*level);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDepthMask(false);
			        GL11.glDisable(GL11.GL_ALPHA_TEST);
			        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			    	Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, colour.getRGB());
			    	GL11.glDepthMask(true);
			        GL11.glEnable(GL11.GL_DEPTH_TEST);
			        GL11.glEnable(GL11.GL_ALPHA_TEST);
				}
			}
		}
	}
	
}
