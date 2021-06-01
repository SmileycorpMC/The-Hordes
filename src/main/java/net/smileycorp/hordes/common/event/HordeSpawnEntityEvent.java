package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class HordeSpawnEntityEvent extends Event {
	
	protected final EntityPlayer player;
	public EntityLiving entity;
	public BlockPos pos;
	
	public HordeSpawnEntityEvent(EntityPlayer player, EntityLiving entity, BlockPos pos) {
		super();
		this.player=player;
		this.entity=entity;
		this.pos=pos;
	}
	
	public EntityPlayer getEventPlayer() {
		return player;
	}
	
	public World getEventWorld() {
		return player.world;
	}

}
