package net.smileycorp.hordes.common.infection.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;

public interface IInfection {

	public int getInfectionCount();

	public void increaseInfection();

	public void loadInfectionCount(IntTag tag);

	public IntTag saveInfectionCount();

	public static class Infection implements IInfection {

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

	public static class Provider implements ICapabilitySerializable<IntTag> {

		protected IInfection impl = new Infection();
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == Hordes.INFECTION ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
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
