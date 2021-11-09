package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

public class HordeEvent extends PlayerEvent {

	protected final World world;
	protected final IOngoingHordeEvent horde;

	protected final int day;

	public HordeEvent(PlayerEntity player, IOngoingHordeEvent horde) {
		super(player);
		world = player.level;
		this.horde = horde;
		day = (int) Math.floor(world.getDayTime()/CommonConfigHandler.dayLength.get());
	}

	public World getEntityWorld() {
		return world;
	}

	public IOngoingHordeEvent getHorde() {
		return horde;
	}

	public int getDay() {
		return day;
	}
}
