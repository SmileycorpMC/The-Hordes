package net.smileycorp.hordes.infection.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface Infection {

	int getInfectionCount();

	void increaseInfection();

	void setInfectionCount(int count);

	class Impl implements Infection {

		protected int count = 0;

		@Override
		public int getInfectionCount() {
			return count;
		}

		@Override
		public void increaseInfection() {
			count++;
		}

		@Override
		public void setInfectionCount(int count) {
			this.count = count;
		}


	}

	class Storage implements IStorage<Infection> {

		@Override
		public NBTBase writeNBT(Capability<Infection> capability, Infection instance, EnumFacing side) {
			return new NBTTagInt(instance.getInfectionCount());
		}

		@Override
		public void readNBT(Capability<Infection> capability, Infection instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagInt) instance.setInfectionCount(((NBTTagInt) nbt).getInt());
		}


	}

	class Provider implements ICapabilityProvider {

		protected final Infection instance = new Impl();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == HordesCapabilities.INFECTION;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == HordesCapabilities.INFECTION ? HordesCapabilities.INFECTION.cast(instance) : null;
		}

	}

}
