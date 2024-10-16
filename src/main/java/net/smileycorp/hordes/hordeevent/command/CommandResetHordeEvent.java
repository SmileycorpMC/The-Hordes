package net.smileycorp.hordes.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;

import java.util.Collection;

public class CommandResetHordeEvent {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("reset")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx))
				.then(Commands.argument("player", EntityArgument.players())
						.executes(ctx -> execute(ctx, EntityArgument.getPlayers(ctx, "player")))));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof Player) return execute(ctx, Lists.newArrayList(source.getPlayerOrException()));
		return 0;
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		for (ServerPlayer player : players) {
			HordeEvent horde = HordeSavedData.getData(source.getLevel()).getEvent(player);
			if (horde != null) {
				horde.reset(player);
				source.getEntity().sendSystemMessage(TextUtils.translatableComponent("commands.hordes.HordeReset.success", null, player.getName()));
				return 1;
			}
		}
		return 0;
	}
}