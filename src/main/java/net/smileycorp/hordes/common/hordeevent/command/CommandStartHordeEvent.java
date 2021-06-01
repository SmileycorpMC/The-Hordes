package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.OngoingHordeEvent;
import net.smileycorp.hordes.common.hordeevent.WorldSaveHordeEvent;

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
		World world = sender.getEntityWorld();
		if (args.length!=1) {
			throw new CommandException("commands."+ModDefinitions.modid+".StartHorde.usage", new Object[] {});
		}
		try {
			int duration = parseInt(args[0], 0);
			server.addScheduledTask(() -> {
				WorldSaveHordeEvent data = WorldSaveHordeEvent.get(world);
				for (OngoingHordeEvent event : data.getEvents()) {
					if (!event.isActive(world)) {
						event.tryStartEvent(duration);
					}
				}
				data.markDirty();
			});
			notifyCommandListener(sender, this, "commands."+ModDefinitions.modid+".StartHorde.success", new Object[] {new TextComponentTranslation(args[0])});
        }
        catch (NumberInvalidException e) {
        	 throw new CommandException("commands."+ModDefinitions.modid+".StartHorde.invalidValue", new Object[] {new TextComponentTranslation(args[0])});
        }
		
	}
 
}
