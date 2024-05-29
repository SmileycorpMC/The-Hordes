package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.IOngoingHordeEvent;

@Cancelable
public class HordeStartWaveEvent extends HordeEvent {

	protected ResourceLocation sound = Constants.HORDE_SOUND;
	protected int count;

	public HordeStartWaveEvent(EntityPlayer player, IOngoingHordeEvent horde, int count) {
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
