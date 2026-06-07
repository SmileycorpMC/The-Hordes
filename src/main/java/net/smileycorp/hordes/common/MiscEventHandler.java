package net.smileycorp.hordes.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.common.entities.EntityHuskPlayer;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.ZombiePlayersConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.integration.oe.EntityDrownedPlayer;

import java.util.Collection;
import java.util.List;

public class MiscEventHandler {

	//send error messages if the logger has errors
	@SubscribeEvent
	public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.player == null) return;
		if (event.player.world.isRemote) return;
		if (HordesLogger.hasErrors()) {
			List<ResourceLocation> scripts = HordesLogger.getErroredScripts();
			TextComponentBase message = scripts.isEmpty() ? new TextComponentTranslation("message.hordes.DataError", HordesLogger.getFiletext()) :
					new TextComponentTranslation("message.hordes.DataErrorScripts", scripts, HordesLogger.getFiletext());
			event.player.sendMessage(message);
		}
	}

	//clear errors on world leave
	@SubscribeEvent
	public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.player == null) return;
		if (event.player.world.isRemote) return;
		if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) return;
		HordesLogger.clearLog(false);
		HordesLogger.clearErrors();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		if (!(entity instanceof EntityPlayer) || entity instanceof FakePlayer || entity.world.isRemote
				|| entity.world.getDifficulty() == EnumDifficulty.PEACEFUL) return;
		if ((entity.isPotionActive(HordesInfection.INFECTED) && InfectionConfig.infectionSpawnsZombiePlayers
				&& InfectionConfig.enableMobInfection) || ZombiePlayersConfig.zombieGraves) {
			ZombifyPlayer cap = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
			if (cap == null) return;
			cap.createZombie();
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void onDrop(PlayerDropsEvent event) {
		if (event.getEntity() instanceof FakePlayer || event.getEntity().world.isRemote
				|| event.getEntity().world.getDifficulty() == EnumDifficulty.PEACEFUL) return;
		EntityPlayer player = event.getEntityPlayer();
		if (!(player.isPotionActive(HordesInfection.INFECTED) && InfectionConfig.enableMobInfection) &! ZombiePlayersConfig.zombieGraves) return;
		ZombifyPlayer cap = player.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null);
		if (cap == null) return;
		PlayerZombie<?> zombie = cap.getZombie();
		if (zombie == null) return;
		if (ZombiePlayersConfig.zombiePlayersStoreItems) {
			Collection<EntityItem> drops = event.getDrops();
			zombie.storeDrops(drops);
			drops.clear();
			event.setCanceled(true);
		}
		zombie.asEntity().enablePersistence();
		player.world.spawnEntity(zombie.asEntity());
		cap.clearZombie();
		player.removePotionEffect(HordesInfection.INFECTED);
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (!(entity instanceof EntityPlayer) || (entity instanceof FakePlayer)) return;
		event.addCapability(Constants.loc("Zombify"), new ZombifyPlayer.Provider((EntityPlayer) entity));
	}

	@SubscribeEvent(receiveCanceled = true)
	public void spawnZombiePlayer(SpawnZombiePlayerEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (Loader.isModLoaded("oe") && player.isInWater() && ZombiePlayersConfig.drownedPlayers) {
			event.setEntityType(EntityDrownedPlayer.class);
			return;
		}
		if (BiomeDictionary.hasType(player.world.getBiome(player.getPosition()), BiomeDictionary.Type.SANDY) && ZombiePlayersConfig.huskPlayers)
			event.setEntityType(EntityHuskPlayer.class);
	}

}
