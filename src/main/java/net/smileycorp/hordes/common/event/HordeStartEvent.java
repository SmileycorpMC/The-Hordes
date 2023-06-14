package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

@Cancelable
public class HordeStartEvent extends HordeEvent {

	protected final BlockPos pos;
	protected String message = Constants.hordeEventStart;

	protected HordeSpawnTable table;
	protected final boolean wasCommand;

	public HordeStartEvent(Player player, IHordeEvent horde, HordeSpawnTable table, boolean wasCommand) {
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
