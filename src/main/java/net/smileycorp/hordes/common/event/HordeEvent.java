package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

public class HordeEvent extends PlayerEvent {

	protected final Level level;
	protected final IOngoingHordeEvent horde;

	protected final int day;

	public HordeEvent(Player player, IOngoingHordeEvent horde) {
		super(player);
		level = player.level;
		this.horde = horde;
		day = (int) Math.floor(level.getDayTime()/CommonConfigHandler.dayLength.get());
	}

	public Level getEntityLevel() {
		return level;
	}

	public IOngoingHordeEvent getHorde() {
		return horde;
	}

	public int getDay() {
		return day;
	}
}
