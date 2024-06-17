package net.smileycorp.hordes.hordeevent.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

public class CommandSpawnWave extends CommandBase {

	@Override
	public String getName() {
		return "spawnHordeWave";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands."+Constants.MODID +".SpawnHordeWave.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length!=1) {
			throw new CommandException("commands."+Constants.MODID +".SpawnHordeWave.usage", new Object[] {});
		}
		try {
			int count = parseInt(args[0], 0);
			if (sender.getCommandSenderEntity() instanceof EntityPlayer) {
				server.addScheduledTask(() -> {
					EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
					WorldDataHordes data = WorldDataHordes.getData(sender.getEntityWorld());
					data.getEvent((EntityPlayerMP) player).spawnWave((EntityPlayerMP) player, count);
				});
			}
			notifyCommandListener(sender, this, "commands."+Constants.MODID +".SpawnHordeWave.success", new Object[0]);
		}
		catch (NumberInvalidException e) {
			throw new CommandException("commands."+Constants.MODID +".SpawnHordeWave.invalidValue", new Object[] {new TextComponentTranslation(args[0])});
		}

	}

}
