package net.smileycorp.hordes.hordeevent.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.commands.HordesCommand;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

public class SubcommandDebugHordeEvent implements HordesCommand.SubCommand {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		server.addScheduledTask(() -> {
			WorldDataHordes data = WorldDataHordes.getData((WorldServer) sender.getEntityWorld());
			HordesLogger.writeToFile(data.getDebugText());
			data.save();
		});
		Entity entity = sender.getCommandSenderEntity();
		if (entity == null) return;
		entity.sendMessage(new TextComponentTranslation("commands."+Constants.MODID +".HordeDebug.success", HordesLogger.getFiletext()));
	}

}
