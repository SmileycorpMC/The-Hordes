package net.smileycorp.hordes.common.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

@Cancelable
public class HordeStartWaveEvent extends HordePlayerEvent {

	protected ResourceLocation sound = Constants.HORDE_SOUND;
	protected int count;

	public HordeStartWaveEvent(Player player, IHordeEvent horde, int count) {
		super(player, horde);
		this.count = count;
	}

	public ResourceLocation getSound() {
		return sound;
	}

	public void setSound(ResourceLocation sound) {
		this.sound = sound;
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
