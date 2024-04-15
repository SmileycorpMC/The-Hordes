package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

import java.util.UUID;

public interface HordeSpawn {
	
	boolean isHordeSpawned();
	
	void setPlayerUUID(String uuid);
	
	String getPlayerUUID();
	
	boolean isSynced();
	
	void setSynced();
	
	StringNBT writeNBT();
	
	void readNBT(StringNBT tag);
	
	static ServerPlayerEntity getHordePlayer(Entity entity) {
		if (entity.level.isClientSide |!(entity instanceof MobEntity)) return null;
		LazyOptional<HordeSpawn> optional = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (!optional.isPresent()) return null;
		HordeSpawn cap = optional.orElseGet(null);
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
		public StringNBT writeNBT() {
			return StringNBT.valueOf(uuid);
		}
		
		@Override
		public void readNBT(StringNBT tag) {
			uuid = tag.getAsString();
		}
		
	}
	
	class Provider implements ICapabilitySerializable<StringNBT> {
		
		protected HordeSpawn impl = new Impl();
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing) {
			return cap == HordesCapabilities.HORDESPAWN ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}
		
		@Override
		public StringNBT serializeNBT() {
			return impl.writeNBT();
		}
		
		@Override
		public void deserializeNBT(StringNBT nbt) {
			impl.readNBT(nbt);
		}
		
	}
	
}
