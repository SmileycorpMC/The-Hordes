package net.smileycorp.hordes.common.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeStartWaveEvent extends HordePlayerEvent implements ICancellableEvent {
	
	protected int count;

	public HordeStartWaveEvent(ServerPlayer player, HordeEvent horde, int count) {
		super(player, horde);
		this.count = count;
	}

	//get the total number of mobs to spawn
	public int getCount() {
		return count;
	}

	//set the total number of mobs to spawn
	public void setCount(int count) {
		this.count = count;
	}

}
