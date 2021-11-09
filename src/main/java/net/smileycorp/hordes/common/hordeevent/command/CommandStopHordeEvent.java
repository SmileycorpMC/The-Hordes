package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

public class CommandStopHordeEvent extends CommandBase {

	@Override
	public String getName() {
		return "stopHordeEvent";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands."+ModDefinitions.MODID+".StopHorde.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		server.addScheduledTask(() -> {
			EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
			if (player.hasCapability(Hordes.HORDE_EVENT, null)) player.getCapability(Hordes.HORDE_EVENT, null).stopEvent(sender.getEntityWorld(), true);
		});
		notifyCommandListener(sender, this, "commands."+ModDefinitions.MODID+".StopHorde.success", new Object[] {});
	}
}
