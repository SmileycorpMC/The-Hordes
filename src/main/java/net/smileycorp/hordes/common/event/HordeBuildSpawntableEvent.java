package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.MutableSpawnTable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeBuildSpawntableEvent extends HordePlayerEvent {


	public MutableSpawnTable spawntable;

	public HordeBuildSpawntableEvent(Player player, HordeSpawnTable spawntable, HordeEvent horde) {
		super(player, horde);
		this.spawntable = MutableSpawnTable.of(spawntable);
	}

	public void setSpawnTable(HordeSpawnTable spawntable) {
		this.spawntable = MutableSpawnTable.of(spawntable);
	}
}
