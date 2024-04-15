package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeStartEvent extends HordePlayerEvent {
	
	protected final BlockPos pos;
	protected String message = Constants.hordeEventStart;
	protected final boolean wasCommand;

	public HordeStartEvent(ServerPlayerEntity player, HordeEvent horde, boolean wasCommand) {
		super(player, horde);
		pos = player.blockPosition();
		this.wasCommand = wasCommand;
	}

	public BlockPos getPlayerPos() {
		return pos;
	}

	//Whether the event was started with a command
	public boolean wasCommand() {
		return wasCommand;
	}

}
