package net.smileycorp.hordes.common.capability;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import org.jetbrains.annotations.Nullable;

public interface ZombifyPlayer {

	PlayerZombie createZombie();
	
	PlayerZombie getZombie();
	
	void clearZombie();
	
	boolean wasZombified();
	
	class Impl implements ZombifyPlayer {
		
		private final Player player;
		private PlayerZombie zombie = null;
		
		public Impl(Player player) {
			this.player = player;
		}
		
		@Override
		public PlayerZombie createZombie() {
			SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, HordesEntities.ZOMBIE_PLAYER.get());
			NeoForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return null;
			EntityType<? extends PlayerZombie> type = event.getEntityType();
			zombie = type.create(player.level());
			zombie.setPlayer(player);
			zombie.asEntity().setPos(player.getX(), player.getY(), player.getZ());
			zombie.asEntity().yBodyRotO = player.yBodyRotO;
			return zombie;
		}
		
		@Override
		public PlayerZombie getZombie() {
			return zombie;
		}
		
		@Override
		public void clearZombie() {
			zombie = null;
		}
		
		@Override
		public boolean wasZombified() {
			return zombie != null;
		}
		
	}

	class Provider implements ICapabilityProvider<Player, Void, ZombifyPlayer> {

		private ZombifyPlayer instance;

		@Override
		public @Nullable ZombifyPlayer getCapability(Player player, Void unused) {
			if (instance == null) instance = new Impl(player);
			return instance;
		}

	}

}