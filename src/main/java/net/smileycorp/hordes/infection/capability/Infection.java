package net.smileycorp.hordes.infection.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public interface Infection {

	int getInfectionCount();

	void increaseInfection();

	void loadInfectionCount(IntTag tag);

	IntTag saveInfectionCount();

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
		public void loadInfectionCount(IntTag tag) {
			count = tag.getAsInt();
		}

		@Override
		public IntTag saveInfectionCount() {
			return IntTag.valueOf(count);
		}

	}

	class Provider implements ICapabilitySerializable<IntTag> {

		protected Infection impl = new Impl();
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == HordesCapabilities.INFECTION ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public IntTag serializeNBT() {
			return impl.saveInfectionCount();
		}

		@Override
		public void deserializeNBT(IntTag nbt) {
			impl.loadInfectionCount(nbt);
		}

	}

}