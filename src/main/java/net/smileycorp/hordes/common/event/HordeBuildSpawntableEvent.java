package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;

public class HordeBuildSpawntableEvent extends HordeEvent {

	protected final EntityPlayer entityPlayer;
	public WeightedOutputs<Class<? extends EntityLiving>> spawntable;
	public BlockPos pos;

	public HordeBuildSpawntableEvent(EntityPlayer player, WeightedOutputs<Class<? extends EntityLiving>> spawntable, BlockPos pos, IOngoingHordeEvent horde) {
		super(player, horde);
		entityPlayer = player;
		this.spawntable = spawntable;
		this.pos=pos;
	}

}
