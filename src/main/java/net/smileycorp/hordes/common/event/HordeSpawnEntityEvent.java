package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordeEvent {

	public Mob entity;
	public BlockPos pos;

	public HordeSpawnEntityEvent(Player player, Mob entity, BlockPos pos, IOngoingHordeEvent horde) {
		super(player, horde);
		this.entity=entity;
		this.pos=pos;
	}

}
