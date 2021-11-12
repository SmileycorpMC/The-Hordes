package net.smileycorp.hordes.common;

import java.util.Collection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.infection.HordesInfection;

@EventBusSubscriber(modid = ModDefinitions.MODID, bus = Bus.MOD)
public class MiscEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity!=null) {
			World world = entity.level;
			if (!world.isClientSide) {
				if (entity instanceof PlayerEntity &!(entity instanceof FakePlayer)) {
					if ((entity.hasEffect(HordesInfection.INFECTED.get()) && CommonConfigHandler.enableMobInfection.get()) || CommonConfigHandler.zombieGraves.get() ||
							(entity.isUnderWater() && CommonConfigHandler.drownedGraves.get())) {
						LazyOptional<IZombifyPlayer> optional = entity.getCapability(Hordes.ZOMBIFY_PLAYER, null);
						if (optional.isPresent()) {
							optional.resolve().get().createZombie((PlayerEntity) entity);
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
				if ((player.hasEffect(HordesInfection.INFECTED.get()) && CommonConfigHandler.enableMobInfection.get()) || CommonConfigHandler.zombieGraves.get()) {
					LazyOptional<IZombifyPlayer> optional = player.getCapability(Hordes.ZOMBIFY_PLAYER, null);
					if (optional.isPresent()) {
						IZombifyPlayer cap = optional.resolve().get();
						MobEntity zombie = cap.getZombie();
						if (zombie!=null) {
							Collection<ItemEntity> drops = event.getDrops();
							((IZombiePlayer)zombie).setInventory(drops);
							zombie.setPersistenceRequired();
							world.addFreshEntity(zombie);
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
			event.addCapability(ModDefinitions.getResource("Infection"), new IZombifyPlayer.Provider());
		}
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HordesInfection.ZOMBIE_PLAYER.get(), ZombieEntity.createAttributes().build());
		event.put(HordesInfection.DROWNED_PLAYER.get(),DrownedEntity.createAttributes().build());
	}

}
