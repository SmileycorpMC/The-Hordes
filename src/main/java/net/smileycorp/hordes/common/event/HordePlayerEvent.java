package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

import java.util.Random;

public class HordePlayerEvent extends Event {

	protected final World world;
	protected final HordeEvent horde;
	protected final int day;
	private final EntityPlayerMP player;
	
	public HordePlayerEvent(EntityPlayerMP player, HordeEvent horde) {
		world = player.world;
		this.horde = horde;
		day = (int) Math.floor(world.getWorldTime() / HordeEventConfig.dayLength);
		this.player = player;
	}

	public World getEntityWorld() {
		return world;
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
	
	public EntityPlayerMP getPlayer() {
		return player;
	}
	
	public EntityLivingBase getEntity() {
		return player;
	}
	
}
