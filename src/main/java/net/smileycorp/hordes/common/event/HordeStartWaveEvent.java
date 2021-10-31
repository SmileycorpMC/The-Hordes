package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@Cancelable
public class HordeStartWaveEvent extends HordeEvent {

	protected ResourceLocation sound = ModDefinitions.HORDE_SOUND;
	protected int count;

	public HordeStartWaveEvent(PlayerEntity player, IOngoingHordeEvent horde, int count) {
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
