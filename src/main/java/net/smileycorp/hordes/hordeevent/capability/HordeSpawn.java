package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

import java.util.UUID;

public interface HordeSpawn {

    boolean isHordeSpawned();

	void setPlayerUUID(String uuid);

	String getPlayerUUID();

	boolean isSynced();

	void setSynced();

	StringTag writeNBT();

	void readNBT(StringTag tag);

	static Player getHordePlayer(Entity entity) {
		if (entity.level().isClientSide |!(entity instanceof Mob)) return null;
		LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (!optional.isPresent()) return null;
		HordeSpawn cap = optional.resolve().get();
		if (!cap.isHordeSpawned()) return null;
		String uuid = cap.getPlayerUUID();
		if (!DataUtils.isValidUUID(uuid)) return null;
		return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
	}

	class Impl implements HordeSpawn {

		private String uuid = "";
		private boolean isSynced;

		@Override
		public boolean isHordeSpawned() {
			return !uuid.isEmpty();
		}

		@Override
		public void setPlayerUUID(String uuid) {
			this.uuid = uuid;
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
		public StringTag writeNBT() {
			return StringTag.valueOf(uuid);
		}

		@Override
		public void readNBT(StringTag tag) {
			uuid = tag.getAsString();
		}

	}

	class Provider implements ICapabilitySerializable<StringTag> {

		protected HordeSpawn impl = new Impl();
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == HordesCapabilities.HORDESPAWN ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public StringTag serializeNBT() {
			return impl.writeNBT();
		}

		@Override
		public void deserializeNBT(StringTag nbt) {
			impl.readNBT(nbt);
		}

	}

}
