package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordePlayerEvent {

	public Mob entity;
	public BlockPos pos;

	public HordeSpawnEntityEvent(ServerPlayer player, Mob entity, BlockPos pos, HordeEvent horde) {
		super(player, horde);
		this.entity = entity;
		this.pos = pos;
	}
	
	@Override
	public Mob getEntity() {
		return entity;
	}

}
