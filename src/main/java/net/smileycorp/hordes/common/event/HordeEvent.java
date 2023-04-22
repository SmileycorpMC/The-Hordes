package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.hordeevent.IOngoingHordeEvent;

public class HordeEvent extends PlayerEvent {

	protected final World world;
	protected final IOngoingHordeEvent horde;

	protected final int day;

	public HordeEvent(EntityPlayer player, IOngoingHordeEvent horde) {
		super(player);
		world = player.world;
		this.horde = horde;
		day = (int) Math.floor(world.getWorldTime()/ConfigHandler.dayLength);
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
