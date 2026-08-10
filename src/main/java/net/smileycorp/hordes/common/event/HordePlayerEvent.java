package net.smileycorp.hordes.common.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordePlayerEvent extends Event {

	protected final Level level;
	protected final HordeEvent horde;
	protected final int day;
	private final ServerPlayer player;
	private final RandomSource rand;
	
	public HordePlayerEvent(ServerPlayer player, HordeEvent horde) {
		level = player.level();
		this.horde = horde;
		day = horde.getDay();
		this.player = player;
		this.rand = horde.getRandom().fork();
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
		return rand;
	}
	
	public ServerPlayer getPlayer() {
		return player;
	}
	
	public LivingEntity getEntity() {
		return player;
	}

	public HordeSpawnData getSpawnData() {
		return horde.getSpawnData();
	}

}
