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
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.commands.HordesCommand;
import net.smileycorp.hordes.config.data.hordeevent.HordeTableLoader;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class SubcommandStartHordeEvent implements HordesCommand.SubCommand {

	@Override
	public List<String> getTabCompletions(MinecraftServer server, String[] args, @Nullable BlockPos pos) {
		if (args.length <= 1)
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		if (args.length == 3)
			return CommandBase.getListOfStringsMatchingLastWord(args, HordeTableLoader.INSTANCE.getSuggestions());
		return Lists.newArrayList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || args.length > 3) {
			throw new CommandException("commands." + Constants.MODID + ".StartHorde.usage");
		}
		try {
			if (args.length == 1) {
				if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
					throw new CommandException("commands.generic.exception");
				}
				execute(server, sender, CommandBase.parseInt(args[0], 0), Lists.newArrayList((EntityPlayerMP) sender.getCommandSenderEntity()), null);
				return;
			}
			execute(server, sender, CommandBase.parseInt(args[1], 0), CommandBase.getPlayers(server, sender, args[0]), args.length == 3 ? new ResourceLocation(args[2]): null);
		}
		catch (NumberInvalidException e) {
			throw new CommandException("commands." + Constants.MODID + ".StartHorde.invalidValue", new TextComponentTranslation(args[0]));
		}
	}

	public static void execute(MinecraftServer server, ICommandSender sender, int length, Collection<EntityPlayerMP> players, ResourceLocation table) throws CommandException {
		server.addScheduledTask(() -> {
			for (EntityPlayerMP player : players) {
				HordeEvent horde = WorldDataHordes.getData((WorldServer) sender.getEntityWorld()).getEvent(player);
				try {
					if (table != null) horde.setSpawntable(HordeTableLoader.INSTANCE.getTable(table));
					horde.tryStartEvent(player, length, true);
				} catch (Exception e) {
					HordesLogger.logError("Failed to run start command", e);
				}
			}
		});
		Entity entity = sender.getCommandSenderEntity();
		if (entity == null) return;
		entity.sendMessage(new TextComponentTranslation("commands." + Constants.MODID + ".StartHorde.success"));
	}

}
