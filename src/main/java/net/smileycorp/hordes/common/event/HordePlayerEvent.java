package net.smileycorp.hordes.common.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordePlayerEvent extends Event {

	protected final Level level;
	protected final HordeEvent horde;
	protected final int day;
	private final ServerPlayer player;
	
	public HordePlayerEvent(ServerPlayer player, HordeEvent horde) {
		level = player.level();
		this.horde = horde;
		day = (int) Math.floor(level.getDayTime() / HordeEventConfig.dayLength.get());
		this.player = player;
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
	
	public ServerPlayer getPlayer() {
		return player;
	}
	
	public LivingEntity getEntity() {
		return player;
	}
	
}
