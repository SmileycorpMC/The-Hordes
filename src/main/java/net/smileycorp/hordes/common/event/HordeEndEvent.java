package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeEndEvent extends HordePlayerEvent {

	protected final BlockPos pos;
	protected String message;
	protected final boolean wasCommand;

	public HordeEndEvent(Player player, HordeEvent horde, boolean wasCommand, String message) {
		super(player, horde);
		pos = player.blockPosition();
		this.wasCommand = wasCommand;
		this.message = message;
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
