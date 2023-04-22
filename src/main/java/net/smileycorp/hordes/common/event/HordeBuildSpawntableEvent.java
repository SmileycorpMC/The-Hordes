package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.hordeevent.IOngoingHordeEvent;

public class HordeBuildSpawntableEvent extends HordeEvent {

	public WeightedOutputs<HordeSpawnEntry> spawntable;

	public HordeBuildSpawntableEvent(EntityPlayer player, WeightedOutputs<HordeSpawnEntry> weightedOutputs, IOngoingHordeEvent horde) {
		super(player, horde);
		spawntable = weightedOutputs;
	}

}
