package net.smileycorp.hordes.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.ZombiePlayersConfig;
import net.smileycorp.hordes.infection.HordesInfection;

import java.util.Collection;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.MOD)
public class MiscEventHandler {
	
	//send error messages if the logger has errors
	@SubscribeEvent
	public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() == null) return;
		if (event.getEntity().level.isClientSide()) return;
		if (HordesLogger.hasErrors()) event.getEntity().sendMessage(
				new TranslationTextComponent("message.hordes.DataError", HordesLogger.getFiletext()), null);
	}
	
	//determine if zombie entity should spawn, and if so create the correct entity and set properties
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (!(entity instanceof PlayerEntity) || entity instanceof FakePlayer || entity.level.isClientSide || entity.level.getDifficulty() == Difficulty.PEACEFUL) return;
		if ((entity.hasEffect(HordesInfection.INFECTED.get()) && InfectionConfig.infectionSpawnsZombiePlayers.get()
				&& InfectionConfig.enableMobInfection.get()) || ZombiePlayersConfig.zombieGraves.get()) {
			LazyOptional<ZombifyPlayer> optional = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (!optional.isPresent()) return;
			optional.orElseGet(null).createZombie((PlayerEntity) entity);
		}
	}
	
	//move items to zombie entity and spawn if one should spawn
	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof PlayerEntity) || event.getEntity() instanceof FakePlayer || event.getEntity().level.isClientSide
				|| event.getEntity().level.getDifficulty() == Difficulty.PEACEFUL) return;
		PlayerEntity player = (PlayerEntity) event.getEntity();
		if ((player.hasEffect(HordesInfection.INFECTED.get()) && InfectionConfig.enableMobInfection.get()) || ZombiePlayersConfig.zombieGraves.get()) {
			LazyOptional<ZombifyPlayer> optional = player.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (!optional.isPresent()) return;
			ZombifyPlayer cap = optional.orElseGet(null);
			PlayerZombie zombie = cap.getZombie();
			if (zombie == null) return;
			if (ZombiePlayersConfig.zombiePlayersStoreItems.get()) {
				Collection<ItemEntity> drops = event.getDrops();
				zombie.storeDrops(drops);
				drops.clear();
				event.setCanceled(true);
			}
			zombie.asEntity().setPersistenceRequired();
			player.level.addFreshEntity(zombie.asEntity());
			cap.clearZombie();
			player.removeEffect(HordesInfection.INFECTED.get());
		}
	}
	
	//attach zombie player provider to players
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof PlayerEntity &!(entity instanceof FakePlayer)) {
			event.addCapability(Constants.loc("Zombify"), new ZombifyPlayer.Provider());
		}
	}
	
	//copy horse inventories if they convert to another entity, useful for copying armor and saddles to zombie horses
	@SubscribeEvent
	public void entityConvert(LivingConversionEvent.Post event) {
		LivingEntity before = event.getEntityLiving();
		if (before.level.isClientSide) return;
		LivingEntity after = event.getOutcome();
		if (before instanceof AbstractHorseEntity && after instanceof AbstractHorseEntity) {
			Inventory beforeInv = ((AbstractHorseEntity)before).inventory;
			Inventory afterInv = ((AbstractHorseEntity)after).inventory;
			for (int i = 0; i < Math.min(beforeInv.getContainerSize(), afterInv.getContainerSize()); i++)
				afterInv.setItem(i, beforeInv.getItem(i).copy());
		}
	}
	
	//register attributes for zombie players
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HordesEntities.ZOMBIE_PLAYER.get(), ZombieEntity.createAttributes().build());
		event.put(HordesEntities.DROWNED_PLAYER.get(), DrownedEntity.createAttributes().build());
		event.put(HordesEntities.HUSK_PLAYER.get(), HuskEntity.createAttributes().build());
	}
	
	@SubscribeEvent(receiveCanceled = true)
	public void spawnZombiePlayer(SpawnZombiePlayerEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.isUnderWater() && ZombiePlayersConfig.drownedPlayers.get()) {
			event.setEntityType(HordesEntities.DROWNED_PLAYER.get());
			return;
		}
		if (player.level.getBiome(player.blockPosition()).containsTag(HordesEntities.HUSK_PLAYER_SPAWN_BIOMES) && ZombiePlayersConfig.huskPlayers.get())
			event.setEntityType(HordesEntities.HUSK_PLAYER.get());
	}
	
}
