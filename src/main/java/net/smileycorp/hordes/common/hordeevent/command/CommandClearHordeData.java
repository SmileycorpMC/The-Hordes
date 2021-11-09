package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.capability.HordeWorldData;

public class CommandClearHordeData extends CommandBase {

	@Override
	public String getName() {
		return "clearHordeData";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands."+ModDefinitions.MODID+".HordeClean.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
        return 1;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		server.addScheduledTask(() -> {
			HordeWorldData.getCleanData(world);
		});
		notifyCommandListener(sender, this, "commands."+ModDefinitions.MODID+".HordeClean.success", new Object[]{});
    }
 
}
