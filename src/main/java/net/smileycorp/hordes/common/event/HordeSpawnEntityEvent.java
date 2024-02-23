package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeSpawnEntityEvent extends HordePlayerEvent implements ICancellableEvent {

	public Mob entity;
	public BlockPos pos;

	public HordeSpawnEntityEvent(Player player, Mob entity, BlockPos pos, HordeEvent horde) {
		super(player, horde);
		this.entity = entity;
		this.pos = pos;
	}

}
