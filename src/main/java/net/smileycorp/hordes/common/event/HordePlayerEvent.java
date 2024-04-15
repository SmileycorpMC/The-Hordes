package net.smileycorp.hordes.common.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

import java.util.Random;

public class HordePlayerEvent extends Event {

	protected final World level;
	protected final HordeEvent horde;
	protected final int day;
	private final ServerPlayerEntity player;
	
	public HordePlayerEvent(ServerPlayerEntity player, HordeEvent horde) {
		level = player.level;
		this.horde = horde;
		day = (int) Math.floor(level.getDayTime() / HordeEventConfig.dayLength.get());
		this.player = player;
	}

	public World getEntityWorld() {
		return level;
	}

	public HordeEvent getHorde() {
		return horde;
	}

	public int getDay() {
		return day;
	}
	
	public Random getRandom() {
		return horde.getRandom();
	}
	
	public ServerPlayerEntity getPlayer() {
		return player;
	}
	
	public LivingEntity getEntity() {
		return player;
	}
	
}
