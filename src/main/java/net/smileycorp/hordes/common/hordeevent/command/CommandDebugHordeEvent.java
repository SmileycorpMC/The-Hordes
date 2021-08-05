package net.smileycorp.hordes.common.hordeevent.command;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.WorldDataHordeEvent;

public class CommandDebugHordeEvent extends CommandBase {

	@Override
	public String getName() {
		return "debugHorde";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands."+ModDefinitions.modid+".HordeDebug.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
        return 0;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		Path path = Paths.get("logs/hordes.log");
		server.addScheduledTask(() -> {
			WorldDataHordeEvent data = WorldDataHordeEvent.getData(world);
			List<String> out = new ArrayList<String>();
			out.add("World time: " + world.getWorldTime());
			out.add("Existing events: {");
			for (OngoingHordeEvent event : data.getEvents()) {
				out.add("	" +event.toString());
				out.addAll(event.getEntityStrings());
			}
			out.add("}");
			try {
				Files.write(path, out, StandardCharsets.UTF_8);
			} catch (Exception e) {}
			data.save();
		});
		notifyCommandListener(sender, this, "commands."+ModDefinitions.modid+".HordeDebug.success", path.toAbsolutePath().toString());
    }
 
}
