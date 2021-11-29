package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;

public interface IHordeSpawn {

	public boolean isHordeSpawned();

	public void setPlayerUUID(String uuid);

	public String getPlayerUUID();

	public boolean isSynced();

	public void setSynced();

	public static class Storage implements IStorage<IHordeSpawn> {

		@Override
		public INBT writeNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, Direction side) {
			return StringNBT.valueOf(instance.getPlayerUUID());
		}

		@Override
		public void readNBT(Capability<IHordeSpawn> capability, IHordeSpawn instance, Direction side, INBT nbt) {
			instance.setPlayerUUID(((StringNBT) nbt).getAsString());
		}


	}

	public static class HordeSpawn implements IHordeSpawn {

		private String uuid = "";
		private boolean isSynced;

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

		@Override
		public boolean isSynced() {
			return isSynced;
		}

		@Override
		public void setSynced() {
			isSynced = true;
		}

	}

	public static class Provider implements ICapabilitySerializable<INBT> {

		protected IHordeSpawn impl = Hordes.HORDESPAWN.getDefaultInstance();

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == Hordes.HORDESPAWN ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public INBT serializeNBT() {
			return Hordes.HORDESPAWN.getStorage().writeNBT(Hordes.HORDESPAWN, impl, null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			Hordes.HORDESPAWN.getStorage().readNBT(Hordes.HORDESPAWN, impl, null, nbt);
		}

	}

}
