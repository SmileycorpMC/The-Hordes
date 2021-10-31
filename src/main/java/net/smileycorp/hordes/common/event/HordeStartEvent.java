package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@Cancelable
public class HordeStartEvent extends HordeEvent {

	protected final BlockPos pos;
	protected String message = ModDefinitions.hordeEventStart;
	protected final boolean wasCommand;

	public HordeStartEvent(PlayerEntity player, IOngoingHordeEvent horde, boolean wasCommand) {
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

	//get the translation key for the start message
	public String getMessage() {
		return message;
	}

	//set the translation key for the start message
	public void setMessage(String message) {
		this.message = message;
	}

}
