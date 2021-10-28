package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;

@Cancelable
public class HordeStartWaveEvent extends HordeEvent {

	protected SoundEvent sound = Hordes.HORDE_SOUND;

	public HordeStartWaveEvent(EntityPlayer player, IOngoingHordeEvent horde) {
		super(player, horde);
	}

	public SoundEvent getSound() {
		return sound;
	}

	public void setSound(SoundEvent sound) {
		this.sound = sound;
	}

}
