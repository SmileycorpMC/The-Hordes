package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

public class CommandStartHordeEvent extends CommandBase {

	@Override
	public String getName() {
		return "startHordeEvent";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands."+ModDefinitions.modid+".StartHorde.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length!=1) {
			throw new CommandException("commands."+ModDefinitions.modid+".StartHorde.usage", new Object[] {});
		}
		try {
			int duration = parseInt(args[0], 0);
			server.addScheduledTask(() -> {
				EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
				if (player.hasCapability(Hordes.HORDE_EVENT, null)) player.getCapability(Hordes.HORDE_EVENT, null).tryStartEvent(duration, true);
			});
			notifyCommandListener(sender, this, "commands."+ModDefinitions.modid+".StartHorde.success", new Object[] {new TextComponentTranslation(args[0])});
		}
		catch (NumberInvalidException e) {
			throw new CommandException("commands."+ModDefinitions.modid+".StartHorde.invalidValue", new Object[] {new TextComponentTranslation(args[0])});
		}

	}

}
