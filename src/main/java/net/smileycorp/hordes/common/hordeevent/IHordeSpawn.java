package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.smileycorp.hordes.common.Hordes;

public interface IHordeSpawn {

	public boolean isHordeSpawned();
	
	public void setPlayerUUID(String uuid);
	
	public String getPlayerUUID();

	public static class Storage implements IStorage<IHordeSpawn> {

		@Override
		public NBTBase writeNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, EnumFacing side) {
			return new NBTTagString(instance.getPlayerUUID());
		}
	
		@Override
		public void readNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, EnumFacing side, NBTBase nbt) {
			instance.setPlayerUUID(((NBTTagString) nbt).getString());
		}
		
		
	}
	
	public static class Implementation implements IHordeSpawn {

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
	
	public static class Provider implements ICapabilitySerializable<NBTBase> {
		
		protected IHordeSpawn instance = Hordes.HORDESPAWN.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == Hordes.HORDESPAWN;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == Hordes.HORDESPAWN ? Hordes.HORDESPAWN.cast(instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return Hordes.HORDESPAWN.getStorage().writeNBT(Hordes.HORDESPAWN, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			Hordes.HORDESPAWN.getStorage().readNBT(Hordes.HORDESPAWN, instance, null, nbt);
		}

	}
 
}
