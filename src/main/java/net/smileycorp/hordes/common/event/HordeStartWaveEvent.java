package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeStartWaveEvent extends HordePlayerEvent {
	
	protected int count;

	public HordeStartWaveEvent(ServerPlayerEntity player, HordeEvent horde, int count) {
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
