package net.smileycorp.hordes.infection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.infection.entities.EntityZombiePlayer;

@EventBusSubscriber(modid=ModDefinitions.modid)
public class InfectionEventHandler {
	
	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		Entity attacker = event.getSource().getImmediateSource();
		World world = entity.world;
		if (!world.isRemote && attacker instanceof EntityZombie) {
			if (/*entity instanceof EntityPlayer ||*/ entity instanceof EntityVillager) {
				entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, 0));
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
	public void onInfect(InfectionDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (entity instanceof EntityPlayer) {
			EntityZombiePlayer zombie = new EntityZombiePlayer((EntityPlayer)entity);
			zombie.setPosition(entity.posX, entity.posY, entity.posZ);
			zombie.renderYawOffset = entity.renderYawOffset;
			world.spawnEntity(zombie);
			sendDeathMessage(entity);
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
				sendDeathMessage(entity);
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
			if (effect.getDuration() < 10000 - 600) {
				int a = effect.getAmplifier();
				if (a < 3) {
					entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, 10000, a+1));
				} else {
					entity.attackEntityFrom(HordesInfection.INFECTION_DAMAGE, entity.getHealth()*2);
					entity.removePotionEffect(HordesInfection.INFECTED);
				}
			}
		}
	}


	private void sendDeathMessage(EntityLivingBase entity) {
		ITextComponent message = new TextComponentTranslation("text.hordes.DeathMessage", entity.getName());
		Entity attacker = entity.getLastDamageSource().getTrueSource();
		if (!(attacker == null || attacker.isDead)) message = new TextComponentTranslation("text.hordes.DeathMessageFighting", entity.getName(), attacker.getName());
		for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
			player.sendMessage(message);
		}
		
	}
}
