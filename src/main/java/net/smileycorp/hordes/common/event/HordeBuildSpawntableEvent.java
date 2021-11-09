package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnEntry;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

public class HordeBuildSpawntableEvent extends HordeEvent {

	public WeightedOutputs<HordeSpawnEntry> spawntable;

	public HordeBuildSpawntableEvent(PlayerEntity player, WeightedOutputs<HordeSpawnEntry> spawntable, IOngoingHordeEvent horde) {
		super(player, horde);
		this.spawntable = spawntable;
	}

}
