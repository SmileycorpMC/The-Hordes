package net.smileycorp.hordes.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;

public interface ZombifyPlayer {
	
	PlayerZombie createZombie(PlayerEntity player);
	
	PlayerZombie getZombie();
	
	void clearZombie();
	
	boolean wasZombified();
	
	class Impl implements ZombifyPlayer {
		
		private PlayerZombie zombie = null;
		
		@Override
		public PlayerZombie createZombie(PlayerEntity player) {
			HordesEntities.ZOMBIE_PLAYER.get();
			SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, HordesEntities.ZOMBIE_PLAYER.get());
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return null;
			EntityType<? extends PlayerZombie> type = event.getEntityType();
			zombie = type.create(player.level);
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
	
	class Provider implements ICapabilityProvider {
		
		protected final ZombifyPlayer impl = new Impl();
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return capability == HordesCapabilities.ZOMBIFY_PLAYER ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}
		
	}
	
}
