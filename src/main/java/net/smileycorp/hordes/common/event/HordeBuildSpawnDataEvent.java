package net.smileycorp.hordes.common.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeBuildSpawnDataEvent extends HordePlayerEvent implements ICancellableEvent {
	
	private final HordeSpawnData spawnData;

	public HordeBuildSpawnDataEvent(ServerPlayer player, HordeEvent horde) {
		super(player, horde);
		spawnData = new HordeSpawnData(horde);
	}

	@Override
	public HordeSpawnData getSpawnData() {
		return spawnData;
	}
	
}
