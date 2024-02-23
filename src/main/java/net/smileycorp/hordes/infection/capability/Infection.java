package net.smileycorp.hordes.infection.capability;

import net.minecraft.nbt.IntTag;

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

}