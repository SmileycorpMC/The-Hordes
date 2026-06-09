package net.smileycorp.hordes.hordeevent.command;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.commands.HordesCommand;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

import java.util.Collection;
import java.util.List;

public class SubCommandStopHordeEvent implements HordesCommand.SubCommand {

	@Override
	public List<String> getTabCompletions(MinecraftServer server, String[] args, BlockPos pos) {
		return args.length <= 1 ?CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Lists.newArrayList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 1) {
			throw new CommandException("commands." + Constants.MODID + ".StopHorde.usage");
		}
		try {
			if (args.length == 0) {
				if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
					throw new CommandException("commands.generic.exception");
				}
				execute(server, sender, Lists.newArrayList((EntityPlayerMP) sender.getCommandSenderEntity()));
				return;
			}
			execute(server, sender, CommandBase.getPlayers(server, sender, args[0]));
		} catch (Exception e) {}
	}

	public static void execute(MinecraftServer server, ICommandSender sender, Collection<EntityPlayerMP> players) throws CommandException {
		server.addScheduledTask(() -> {
			for (EntityPlayerMP player : players)
				WorldDataHordes.getData((WorldServer) sender.getEntityWorld()).getEvent(player).stopEvent(player, true);
		});
		Entity entity = sender.getCommandSenderEntity();
		if (entity == null) return;
		entity.sendMessage(new TextComponentTranslation("commands."+Constants.MODID +".StopHorde.success"));
	}

}
