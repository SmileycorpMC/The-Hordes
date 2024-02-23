package net.smileycorp.hordes.common.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordePlayerEvent extends PlayerEvent {

	protected final Level level;
	protected final HordeEvent horde;

	protected final int day;

	public HordePlayerEvent(Player player, HordeEvent horde) {
		super(player);
		level = player.level();
		this.horde = horde;
		day = (int) Math.floor(level.getDayTime() / HordeEventConfig.dayLength.get());
	}

	public Level getEntityWorld() {
		return level;
	}

	public HordeEvent getHorde() {
		return horde;
	}

	public int getDay() {
		return day;
	}
	
	public RandomSource getRandom() {
		return horde.getRandom();
	}
	
}
