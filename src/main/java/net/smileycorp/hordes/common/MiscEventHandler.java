package net.smileycorp.hordes.common;

import net.minecraft.world.Difficulty;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
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
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.ZombiePlayersConfig;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.infection.HordesInfection;

import java.util.Collection;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.MOD)
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
		if ((entity.hasEffect(HordesInfection.INFECTED.get()) && InfectionConfig.infectionSpawnsZombiePlayers.get()
				&& InfectionConfig.enableMobInfection.get()) || ZombiePlayersConfig.zombieGraves.get()) {
			LazyOptional<ZombifyPlayer> optional = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (!optional.isPresent()) return;
			optional.orElseGet(null).createZombie((Player) entity);
		}
	}

	//move items to zombie entity and spawn if one should spawn
	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getEntity() instanceof FakePlayer || event.getEntity().level().isClientSide
				|| event.getEntity().level().getDifficulty() == Difficulty.PEACEFUL) return;
		Player player = (Player) event.getEntity();
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
			player.level().addFreshEntity(zombie.asEntity());
			cap.clearZombie();
			player.removeEffect(HordesInfection.INFECTED.get());
		}
	}

	//attach zombie player provider to players
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player &!(entity instanceof FakePlayer)) {
			event.addCapability(Constants.loc("Zombify"), new ZombifyPlayer.Provider());
		}
	}

	//copy horse inventories if they convert to another entity, useful for copying armor and saddles to zombie horses
	@SubscribeEvent
	public void entityConvert(LivingConversionEvent.Post event) {
		LivingEntity before = event.getEntity();
		if (before.level().isClientSide) return;
		LivingEntity after = event.getOutcome();
		if (before instanceof AbstractHorse && after instanceof AbstractHorse) {
			SimpleContainer beforeInv = ((AbstractHorse)before).inventory;
			SimpleContainer afterInv = ((AbstractHorse)after).inventory;
			for (int i = 0; i < Math.min(beforeInv.getContainerSize(), afterInv.getContainerSize()); i++) {
				afterInv.setItem(i, beforeInv.getItem(i).copy());
			}
		}
	}

	//register attributes for zombie players
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HordesEntities.ZOMBIE_PLAYER.get(), Zombie.createAttributes().build());
		event.put(HordesEntities.DROWNED_PLAYER.get(), Drowned.createAttributes().build());
		event.put(HordesEntities.HUSK_PLAYER.get(), Husk.createAttributes().build());
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
