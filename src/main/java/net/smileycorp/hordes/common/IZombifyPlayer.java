package net.smileycorp.hordes.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;

public interface IZombifyPlayer {

	public EntityZombiePlayer createZombie();
	
	public EntityZombiePlayer getZombie();
	
	public void clearZombie();

	public static class Storage implements IStorage<IZombifyPlayer> {

		@Override
		public NBTBase writeNBT(Capability<IZombifyPlayer> capability, IZombifyPlayer instance, EnumFacing side) {
			return null;
		}
	
		@Override
		public void readNBT(Capability<IZombifyPlayer> capability, IZombifyPlayer instance, EnumFacing side, NBTBase nbt) {}
		
		
	}
	
	public static class Implementation implements IZombifyPlayer {
		
		private final EntityPlayer player;
		private EntityZombiePlayer zombie = null;
		
		public Implementation() {
			player = null;
		}
		
		public Implementation(EntityPlayer player) {
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
			zombie=null;
		}

	}
	
	public static class Provider implements ICapabilityProvider {
		
		protected final IZombifyPlayer instance;

		public Provider(EntityPlayer player) {
			instance = new Implementation(player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == Hordes.ZOMBIFY_PLAYER;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == Hordes.ZOMBIFY_PLAYER ? Hordes.ZOMBIFY_PLAYER.cast(instance) : null;
		}

	}
 
}
