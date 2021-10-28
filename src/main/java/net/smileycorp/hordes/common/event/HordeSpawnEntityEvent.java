package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordeEvent {

	public EntityLiving entity;
	public BlockPos pos;

	public HordeSpawnEntityEvent(EntityPlayer player, EntityLiving entity, BlockPos pos, IOngoingHordeEvent horde) {
		super(player, horde);
		this.entity=entity;
		this.pos=pos;
	}

}
