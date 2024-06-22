package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
	
	static ServerPlayer getHordePlayer(Entity entity) {
		if (entity.level().isClientSide |!(entity instanceof Mob)) return null;
		HordeSpawn hordespawn = entity.getCapability(HordesCapabilities.HORDESPAWN);
		if (hordespawn == null) return null;
		if (!hordespawn.isHordeSpawned()) return null;
		String uuid = hordespawn.getPlayerUUID();
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
	
}