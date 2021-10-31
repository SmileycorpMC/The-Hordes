package net.smileycorp.hordes.common.event;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordeEvent {

	public MobEntity entity;
	public BlockPos pos;

	public HordeSpawnEntityEvent(PlayerEntity player, MobEntity entity, BlockPos pos, IOngoingHordeEvent horde) {
		super(player, horde);
		this.entity=entity;
		this.pos=pos;
	}

}
