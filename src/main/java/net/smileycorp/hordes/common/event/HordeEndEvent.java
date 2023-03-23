package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

public class HordeEndEvent extends HordeEvent {

	protected final BlockPos pos;
	protected String message = Constants.hordeEventEnd;
	protected final boolean wasCommand;

	public HordeEndEvent(Player player, IHordeEvent horde, boolean wasCommand) {
		super(player, horde);
		pos = player.blockPosition();
		this.wasCommand = wasCommand;
	}

	public BlockPos getPlayerPos() {
		return pos;
	}

	//Whether the event was ended due to a command
	public boolean wasCommand() {
		return wasCommand;
	}

	//get the translation key for the end message
	public String getMessage() {
		return message;
	}

	//set the translation key for the end message
	public void setMessage(String message) {
		this.message = message;
	}
}
