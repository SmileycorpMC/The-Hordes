package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public interface IZombifyPlayer {

	public IZombiePlayer createZombie();

	public IZombiePlayer getZombie();

	public void clearZombie();

	public static class Storage implements IStorage<IZombifyPlayer> {

		@Override
		public INBT writeNBT(Capability<IZombifyPlayer> capability, IZombifyPlayer instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IZombifyPlayer> capability, IZombifyPlayer instance, Direction side, INBT nbt) {}


	}

	public static class Provider implements ICapabilityProvider {

		protected final IZombifyPlayer impl;

		public Provider(PlayerEntity player) {
			impl = new ZombifyPlayer(player);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return capability == Hordes.ZOMBIFY_PLAYER ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

	}

}
