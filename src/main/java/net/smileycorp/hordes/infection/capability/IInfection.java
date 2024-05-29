package net.smileycorp.hordes.infection.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface IInfection {

	public int getInfectionCount();

	public void increaseInfection();

	public void setInfectionCount(int count);

	public static class Implementation implements IInfection {

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

	public static class Storage implements IStorage<IInfection> {

		@Override
		public NBTBase writeNBT(Capability<IInfection> capability, IInfection instance, EnumFacing side) {
			return new NBTTagInt(instance.getInfectionCount());
		}

		@Override
		public void readNBT(Capability<IInfection> capability, IInfection instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagInt) instance.setInfectionCount(((NBTTagInt) nbt).getInt());
		}


	}

	public static class Provider implements ICapabilityProvider {

		protected final IInfection instance = new Implementation();

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
