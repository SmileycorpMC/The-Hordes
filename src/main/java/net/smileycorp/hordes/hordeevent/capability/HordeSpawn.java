package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface HordeSpawn {

	boolean isHordeSpawned();
	
	public void setPlayerUUID(String uuid);
	
	String getPlayerUUID();

	class Storage implements IStorage<HordeSpawn> {

		@Override
		public NBTBase writeNBT(Capability<HordeSpawn> capability, net.smileycorp.hordes.hordeevent.capability.HordeSpawn instance, EnumFacing side) {
			return new NBTTagString(instance.getPlayerUUID());
		}
	
		@Override
		public void readNBT(Capability<HordeSpawn> capability, net.smileycorp.hordes.hordeevent.capability.HordeSpawn instance, EnumFacing side, NBTBase nbt) {
			instance.setPlayerUUID(((NBTTagString) nbt).getString());
		}
		
		
	}
	
	class Impl implements net.smileycorp.hordes.hordeevent.capability.HordeSpawn {

		private String uuid = "";
		
		@Override
		public boolean isHordeSpawned() {
			return !uuid.isEmpty();
		}

		@Override
		public void setPlayerUUID(String uuid) {
			this.uuid=uuid;
		}

		@Override
		public String getPlayerUUID() {
			return uuid;
		}

	}
	
	class Provider implements ICapabilitySerializable<NBTBase> {
		
		protected HordeSpawn instance = new Impl();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDESPAWN;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HordesCapabilities.HORDESPAWN ? HordesCapabilities.HORDESPAWN.cast(instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return HordesCapabilities.HORDESPAWN.getStorage().writeNBT(HordesCapabilities.HORDESPAWN, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			HordesCapabilities.HORDESPAWN.getStorage().readNBT(HordesCapabilities.HORDESPAWN, instance, null, nbt);
		}

	}
 
}
