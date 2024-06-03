package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeBuildSpawnDataEvent extends HordePlayerEvent {
	
	private final HordeSpawnData spawnData;

	public HordeBuildSpawnDataEvent(EntityPlayerMP player, HordeEvent horde) {
		super(player, horde);
		spawnData = new HordeSpawnData(horde);
	}

	public HordeSpawnData getSpawnData() {
		return spawnData;
	}
	
}
