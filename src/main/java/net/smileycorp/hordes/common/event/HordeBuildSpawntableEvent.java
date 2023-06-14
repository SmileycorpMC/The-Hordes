package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.common.hordeevent.MutableSpawnTable;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

public class HordeBuildSpawntableEvent extends HordePlayerEvent {


	public MutableSpawnTable spawntable;

	public HordeBuildSpawntableEvent(Player player, HordeSpawnTable spawntable, IHordeEvent horde) {
		super(player, horde);
		this.spawntable = MutableSpawnTable.of(spawntable);
	}

	public void setSpawnTable(HordeSpawnTable spawntable) {
		this.spawntable = MutableSpawnTable.of(spawntable);
	}
}
