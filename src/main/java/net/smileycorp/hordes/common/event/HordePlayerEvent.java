package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

public class HordePlayerEvent extends PlayerEvent {

	protected final Level level;
	protected final IHordeEvent horde;

	protected final int day;

	public HordePlayerEvent(Player player, IHordeEvent horde) {
		super(player);
		level = player.level;
		this.horde = horde;
		day = (int) Math.floor(level.getDayTime()/CommonConfigHandler.dayLength.get());
	}

	public Level getEntityWorld() {
		return level;
	}

	public IHordeEvent getHorde() {
		return horde;
	}

	public int getDay() {
		return day;
	}
}
