package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.WorldDataHordeEvent;

public class CommandStopHordeEvent extends CommandBase {

	@Override
	public String getName() {
		return "stopHordeEvent";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands."+ModDefinitions.modid+".StopHorde.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
        return 2;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		server.addScheduledTask(() -> {
			WorldDataHordeEvent data = WorldDataHordeEvent.get(world);
			for (OngoingHordeEvent event : data.getEvents()) {
				if (event.isActive(world)) {
					event.stopEvent(world, true);
				}
			}
			data.save();
		});
		notifyCommandListener(sender, this, "commands."+ModDefinitions.modid+".StopHorde.success", new Object[] {});
	}
}
