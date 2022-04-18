package net.smileycorp.hordes.common.hordeevent.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;

public interface IHordeSpawn {

	public boolean isHordeSpawned();

	public void setPlayerUUID(String uuid);

	public String getPlayerUUID();

	public boolean isSynced();

	public void setSynced();

	public Tag writeNBT();

	public void readNBT(Tag nbt);

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

		@Override
		public Tag writeNBT() {
			return StringTag.valueOf(uuid);
		}

		@Override
		public void readNBT(Tag nbt) {
			uuid = ((StringTag) nbt).getAsString();
		}

	}

	public static class Provider implements ICapabilitySerializable<Tag> {

		protected IHordeSpawn impl = new HordeSpawn();

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == Hordes.HORDESPAWN ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public Tag serializeNBT() {
			return impl.writeNBT();
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			impl.readNBT(nbt);
		}

	}

}
