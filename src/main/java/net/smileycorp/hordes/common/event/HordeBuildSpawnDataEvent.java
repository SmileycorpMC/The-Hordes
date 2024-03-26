package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeBuildSpawnDataEvent extends HordePlayerEvent {
	
	private final HordeSpawnData spawnData;

	public HordeBuildSpawnDataEvent(Player player, HordeEvent horde) {
		super(player, horde);
		spawnData = new HordeSpawnData(horde);
	}

	public HordeSpawnData getSpawnData() {
		return spawnData;
	}
	
}
