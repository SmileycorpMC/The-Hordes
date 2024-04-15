package net.smileycorp.hordes.infection.capability;

import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface Infection {

	int getInfectionCount();

	void increaseInfection();

	void loadInfectionCount(IntNBT tag);

	IntNBT saveInfectionCount();

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
		public void loadInfectionCount(IntNBT tag) {
			count = tag.getAsInt();
		}

		@Override
		public IntNBT saveInfectionCount() {
			return IntNBT.valueOf(count);
		}

	}

	class Provider implements ICapabilitySerializable<IntNBT> {

		protected Infection impl = new Impl();
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == HordesCapabilities.INFECTION ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public IntNBT serializeNBT() {
			return impl.saveInfectionCount();
		}

		@Override
		public void deserializeNBT(IntNBT nbt) {
			impl.loadInfectionCount(nbt);
		}

	}

}