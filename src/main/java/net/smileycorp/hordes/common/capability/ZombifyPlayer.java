package net.smileycorp.hordes.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;

public interface ZombifyPlayer {

	Mob createZombie(Player player);

	Mob getZombie();

	void clearZombie();

	boolean wasZombified();

	class Impl implements ZombifyPlayer {

		private Mob zombie = null;

		@Override
		public Mob createZombie(Player player) {
			EntityType<? extends PlayerZombie> type = (player.isUnderWater() && (CommonConfigHandler.drownedPlayers.get() || CommonConfigHandler.drownedGraves.get()))
					? HordesEntities.DROWNED_PLAYER.get() : HordesEntities.ZOMBIE_PLAYER.get();
			SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, type);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return null;
			type = event.getEntityType();
			zombie = (Mob) type.create(player.level());
			((PlayerZombie) zombie).setPlayer(player);
			zombie.setPos(player.getX(), player.getY(), player.getZ());
			zombie.yBodyRotO = player.yBodyRotO;
			return zombie;
		}

		@Override
		public Mob getZombie() {
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
