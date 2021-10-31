package net.smileycorp.hordes.common;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;
import net.smileycorp.hordes.infection.HordesInfection;

public class MiscEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity!=null) {
			World world = entity.level;
			if (!world.isClientSide) {
				if (entity instanceof PlayerEntity &!(entity instanceof FakePlayer)) {
					if ((entity.isPotionActive(HordesInfection.INFECTED) && ConfigHandler.enableMobInfection) || ConfigHandler.zombieGraves) {
						if (entity.hasCapability(Hordes.ZOMBIFY_PLAYER, null)) {
							entity.getCapability(Hordes.ZOMBIFY_PLAYER, null).createZombie();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(LivingDropsEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntity();
			World world = player.level;
			if (!world.isClientSide &!(player instanceof FakePlayer)) {
				if ((player.hasEffect(HordesInfection.INFECTED) && ConfigHandler.enableMobInfection) || ConfigHandler.zombieGraves) {
					if (player.hasCapability(Hordes.ZOMBIFY_PLAYER, null)) {
						IZombifyPlayer cap = player.getCapability(Hordes.ZOMBIFY_PLAYER, null);
						ZombiePlayerEntity zombie = cap.getZombie();
						if (zombie!=null) {
							List<EntityItem> drops = event.getDrops();
							zombie.setInventory(drops);
							zombie.enablePersistence();
							world.spawnEntity(zombie);
							drops.clear();
							cap.clearZombie();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof PlayerEntity &!(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("Infection"), new IZombifyPlayer.Provider((PlayerEntity) entity));
		}
	}
}
