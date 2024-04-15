package net.smileycorp.hordes.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;

import java.util.Collection;

public class CommandStopHordeEvent {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
			command.then(Commands.literal("stop")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx))
				.then(Commands.argument("player", EntityArgument.players())
						.executes(ctx -> execute(ctx, EntityArgument.getPlayers(ctx, "player")))));
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		if (source.getEntity() instanceof PlayerEntity) return execute(ctx, Lists.newArrayList(source.getPlayerOrException()));
		return 0;
	}

	public static int execute(CommandContext<CommandSource> ctx, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		for (ServerPlayerEntity player : players) {
			HordeEvent horde = HordeSavedData.getData(source.getLevel()).getEvent(player);
			if (horde != null) {
				horde.stopEvent(player, true);
				return 1;
			}
		}
		return 0;
	}
	
}
