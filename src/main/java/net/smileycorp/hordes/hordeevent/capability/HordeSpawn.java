package net.smileycorp.hordes.hordeevent.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface HordeSpawn {

	boolean isHordeSpawned();
	
	void setPlayerUUID(String uuid);
	
	String getPlayerUUID();
	
	boolean isSynced();
	
	void setSynced();
	
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

		private final Entity entity;
		private boolean isSynced;

		public Impl(Entity entity) {
			this.entity = entity;
		}

		@Override
		public boolean isHordeSpawned() {
			return !getPlayerUUID().isEmpty();
		}
		
		@Override
		public void setPlayerUUID(String uuid) {
			entity.setData(HordesCapabilities.HORDE_SPAWN_PLAYER, uuid);
		}
		
		@Override
		public String getPlayerUUID() {
			return entity.getData(HordesCapabilities.HORDE_SPAWN_PLAYER);
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


	class Provider implements ICapabilityProvider<Entity, Void, HordeSpawn> {

		private HordeSpawn instance;

		@Override
		public @Nullable HordeSpawn getCapability(Entity entity, Void unused) {
			if (!(entity instanceof Mob)) return null;
			if (instance == null) instance = new HordeSpawn.Impl(entity);
			return instance;
		}

	}
	
}