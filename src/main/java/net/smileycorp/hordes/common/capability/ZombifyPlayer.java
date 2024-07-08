package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;

public interface ZombifyPlayer {

	EntityZombiePlayer createZombie();
	
	EntityZombiePlayer getZombie();
	
	void clearZombie();
	
	boolean wasZombified();

	class Storage implements IStorage<ZombifyPlayer> {

		@Override
		public NBTBase writeNBT(Capability<ZombifyPlayer> capability, ZombifyPlayer instance, EnumFacing side) {
			return null;
		}
	
		@Override
		public void readNBT(Capability<ZombifyPlayer> capability, ZombifyPlayer instance, EnumFacing side, NBTBase nbt) {}
		
		
	}
	
	class Impl implements ZombifyPlayer {
		
		private final EntityPlayer player;
		private EntityZombiePlayer zombie = null;
		
		public Impl() {
			player = null;
		}
		
		public Impl(EntityPlayer player) {
			this.player=player;
		}

		@Override
		public EntityZombiePlayer createZombie() {
			zombie = new EntityZombiePlayer(player);
			zombie.setPosition(player.posX, player.posY, player.posZ);
			zombie.renderYawOffset = player.renderYawOffset;
			return zombie;
		}

		@Override
		public EntityZombiePlayer getZombie() {
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
	
	public static class Provider implements ICapabilityProvider {
		
		protected final ZombifyPlayer instance;

		public Provider(EntityPlayer player) {
			instance = new Impl(player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HordesCapabilities.ZOMBIFY_PLAYER;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HordesCapabilities.ZOMBIFY_PLAYER ? HordesCapabilities.ZOMBIFY_PLAYER.cast(instance) : null;
		}

	}
 
}
