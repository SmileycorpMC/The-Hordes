package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;

public interface ZombifyPlayer {

	PlayerZombie<?> createZombie();
	
	PlayerZombie<?> getZombie();
	
	void clearZombie();
	
	boolean wasZombified();
	
	class Impl implements ZombifyPlayer {
		
		private final EntityPlayer player;
		private PlayerZombie<?> zombie = null;
		
		public Impl() {
			player = null;
		}
		
		public Impl(EntityPlayer player) {
			this.player = player;
		}

		@Override
		public PlayerZombie<?> createZombie() {
			SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, EntityZombiePlayer.class);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return null;
			try {
				zombie = event.getEntityType().getConstructor(World.class).newInstance(player.world);
				zombie.setPlayer(player);
				zombie.asEntity().setPosition(player.posX, player.posY, player.posZ);
				zombie.asEntity().renderYawOffset = player.renderYawOffset;
				return zombie;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public PlayerZombie<?> getZombie() {
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

	class Storage implements IStorage<ZombifyPlayer> {

		@Override
		public NBTBase writeNBT(Capability<ZombifyPlayer> capability, ZombifyPlayer instance, EnumFacing side) {
			return null;
		}

		@Override
		public void readNBT(Capability<ZombifyPlayer> capability, ZombifyPlayer instance, EnumFacing side, NBTBase nbt) {}


	}
	
	class Provider implements ICapabilityProvider {
		
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
