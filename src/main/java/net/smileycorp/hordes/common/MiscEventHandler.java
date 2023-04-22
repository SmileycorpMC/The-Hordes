package net.smileycorp.hordes.common;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.infection.HordesInfection;

public class MiscEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (entity!=null) {
			World world = entity.world;
			if (!world.isRemote) {
				if (entity instanceof EntityPlayer &!(entity instanceof FakePlayer)) {
					if ((entity.isPotionActive(HordesInfection.INFECTED) && ConfigHandler.enableMobInfection && ConfigHandler.infectionSpawnsZombiePlayers) || ConfigHandler.zombieGraves) {
						if (entity.hasCapability(Hordes.ZOMBIFY_PLAYER, null)) {
							entity.getCapability(Hordes.ZOMBIFY_PLAYER, null).createZombie();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(PlayerDropsEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player!=null &!(player instanceof FakePlayer)) {
			World world = player.world;
			if (!world.isRemote) {
				if ((player.isPotionActive(HordesInfection.INFECTED) && ConfigHandler.enableMobInfection && ConfigHandler.infectionSpawnsZombiePlayers) || ConfigHandler.zombieGraves) {
					if (player.hasCapability(Hordes.ZOMBIFY_PLAYER, null)) {
						IZombifyPlayer cap = player.getCapability(Hordes.ZOMBIFY_PLAYER, null);
						EntityZombiePlayer zombie = cap.getZombie();
						if (zombie!=null) {
							List<EntityItem> drops = event.getDrops();
							zombie.setInventory(drops);
							zombie.enablePersistence();
							world.spawnEntity(zombie);
							drops.clear();
							cap.clearZombie();
							player.removePotionEffect(HordesInfection.INFECTED);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!entity.hasCapability(Hordes.HORDESPAWN, null) && entity instanceof EntityPlayer &!(entity instanceof FakePlayer)) {
			event.addCapability(Constants.loc("Infection"), new IZombifyPlayer.Provider((EntityPlayer) entity));
		}
	}
}
