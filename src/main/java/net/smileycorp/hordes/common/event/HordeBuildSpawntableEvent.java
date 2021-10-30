package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.hordeevent.IOngoingHordeEvent;

public class HordeBuildSpawntableEvent extends HordeEvent {

	public WeightedOutputs<Class<? extends EntityLiving>> spawntable;

	public HordeBuildSpawntableEvent(EntityPlayer player, WeightedOutputs<Class<? extends EntityLiving>> spawntable, IOngoingHordeEvent horde) {
		super(player, horde);
		this.spawntable = spawntable;
	}

}
