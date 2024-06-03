package net.smileycorp.hordes.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommandDebugHordeEvent extends CommandBase {

	@Override
	public String getName() {
		return "debugHorde";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands."+Constants.MODID +".HordeDebug.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return -1;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Path path = Paths.get("logs/hordes.log");
		server.addScheduledTask(() -> {
			WorldDataHordes data = WorldDataHordes.getData(sender.getEntityWorld());
			List<String> out = data.getDebugText();
			try {
				Files.write(path, out, StandardCharsets.UTF_8);
			} catch (Exception e) {}
			data.save();
		});
		notifyCommandListener(sender, this, "commands."+Constants.MODID +".HordeDebug.success", path.toAbsolutePath().toString());
	}

}
