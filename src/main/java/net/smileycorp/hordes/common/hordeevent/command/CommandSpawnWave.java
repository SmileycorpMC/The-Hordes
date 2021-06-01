package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.WorldSaveHordeEvent;

public class CommandSpawnWave extends CommandBase {

	@Override
	public String getName() {
		return "spawnHordeWave";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands."+ModDefinitions.modid+".SpawnHordeWave.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
        return 2;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		if (args.length!=1) {
			throw new CommandException("commands."+ModDefinitions.modid+".SpawnHordeWave.usage", new Object[] {});
		}
		try {
			int count = parseInt(args[0], 0);
			if (sender.getCommandSenderEntity() instanceof EntityPlayer) {
				server.addScheduledTask(() -> {
					EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
					WorldSaveHordeEvent data = WorldSaveHordeEvent.get(world);
					data.getEventForPlayer(player).spawnWave(world, count);
					data.markDirty();
				});
			}
			notifyCommandListener(sender, this, "commands."+ModDefinitions.modid+".SpawnHordeWave.success", new Object[0]);
        }
        catch (NumberInvalidException e) {
        	 throw new CommandException("commands."+ModDefinitions.modid+".SpawnHordeWave.invalidValue", new Object[] {new TextComponentTranslation(args[0])});
        }
		
	}
 
}
