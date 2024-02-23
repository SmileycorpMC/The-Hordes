package net.smileycorp.hordes.common.capability;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;

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
			HordesEntities.ZOMBIE_PLAYER.get();
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

}
