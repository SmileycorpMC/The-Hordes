package net.smileycorp.hordes.common.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

import java.util.Collection;

public class CommandStartHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("startHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.then(Commands.argument("length", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length"))).then(Commands.argument("player", EntityArgument.players())
								.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length"), EntityArgument.getPlayers(ctx, "player")))));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int length) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof Player) return execute(ctx, length, Lists.newArrayList(source.getPlayerOrException()));
		return 0;
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int length, Collection<ServerPlayer> players) throws CommandSyntaxException {
		for (Player player : players) {
			LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().tryStartEvent(player, length, true);
			}
		}
		return 1;
	}

}
