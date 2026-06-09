package net.smileycorp.hordes.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.commands.HordesCommand;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

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
