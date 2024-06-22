package net.smileycorp.hordes.common;

import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.ZombiePlayersConfig;
import net.smileycorp.hordes.infection.HordesInfection;

import java.util.Collection;

public class MiscEventHandler {

	//send error messages if the logger has errors
	@SubscribeEvent
	public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() == null) return;
		if (event.getEntity().level().isClientSide()) return;
		if (HordesLogger.hasErrors()) event.getEntity().sendSystemMessage(
				TextUtils.translatableComponent("message.hordes.DataError", null, HordesLogger.getFiletext()));
	}

	//determine if zombie entity should spawn, and if so create the correct entity and set properties
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (!(entity instanceof Player) || entity instanceof FakePlayer || entity.level().isClientSide || entity.level().getDifficulty() == Difficulty.PEACEFUL) return;
		if ((entity.hasEffect(HordesInfection.INFECTED) && InfectionConfig.infectionSpawnsZombiePlayers.get()
				&& InfectionConfig.enableMobInfection.get()) || ZombiePlayersConfig.zombieGraves.get()) {
			ZombifyPlayer cap = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (cap == null) return;
			cap.createZombie();
		}
	}

	//move items to zombie entity and spawn if one should spawn
	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getEntity() instanceof FakePlayer || event.getEntity().level().isClientSide
				|| event.getEntity().level().getDifficulty() == Difficulty.PEACEFUL) return;
		Player player = (Player) event.getEntity();
		if ((player.hasEffect(HordesInfection.INFECTED) && InfectionConfig.enableMobInfection.get()) || ZombiePlayersConfig.zombieGraves.get()) {
			ZombifyPlayer cap = player.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (cap == null) return;
			PlayerZombie zombie = cap.getZombie();
			if (zombie == null) return;
			if (ZombiePlayersConfig.zombiePlayersStoreItems.get()) {
				Collection<ItemEntity> drops = event.getDrops();
				zombie.storeDrops(drops);
				drops.clear();
				event.setCanceled(true);
			}
			zombie.asEntity().setPersistenceRequired();
			player.level().addFreshEntity(zombie.asEntity());
			cap.clearZombie();
			player.removeEffect(HordesInfection.INFECTED);
		}
	}

	

	//copy horse inventories if they convert to another entity, useful for copying armor and saddles to zombie horses
	@SubscribeEvent
	public void entityConvert(LivingConversionEvent.Post event) {
		LivingEntity before = event.getEntity();
		if (before.level().isClientSide) return;
		LivingEntity after = event.getOutcome();
		if (before instanceof AbstractHorse && after instanceof AbstractHorse) {
			Container beforeInv = ((AbstractHorse) before).getInventory();
			Container afterInv = ((AbstractHorse) after).getInventory();
			for (int i = 0; i < Math.min(beforeInv.getContainerSize(), afterInv.getContainerSize()); i++) {
				afterInv.setItem(i, beforeInv.getItem(i).copy());
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void spawnZombiePlayer(SpawnZombiePlayerEvent event) {
		Player player = event.getEntity();
		if (player.isUnderWater() && ZombiePlayersConfig.drownedPlayers.get()) {
			event.setEntityType(HordesEntities.DROWNED_PLAYER.get());
			return;
		}
		if (player.level().getBiome(player.blockPosition()).is(HordesEntities.HUSK_PLAYER_SPAWN_BIOMES) && ZombiePlayersConfig.huskPlayers.get())
			event.setEntityType(HordesEntities.HUSK_PLAYER.get());
	}

}
