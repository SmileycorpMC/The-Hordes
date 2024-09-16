package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

import java.util.Collection;

public class HordeEndEvent extends HordePlayerEvent {

	protected final BlockPos pos;
	private final Collection<String> commands;
	protected String message;
	protected final boolean wasCommand;

	public HordeEndEvent(ServerPlayer player, HordeEvent horde, boolean wasCommand, String message, Collection<String> commands) {
		super(player, horde);
		pos = player.blockPosition();
		this.wasCommand = wasCommand;
		this.message = message;
		this.commands = commands;
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
	
	//modify the reward commands
	public Collection<String> getCommands() {
		return commands;
	}
	
}
