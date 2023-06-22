package net.smileycorp.hordes.common.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.common.hordeevent.capability.HordeSavedData;

import java.util.Collection;

public class CommandResetHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("resetHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx))
				.then(Commands.argument("player", EntityArgument.players())
						.executes(ctx -> execute(ctx, EntityArgument.getPlayers(ctx, "player"))));
		dispatcher.register(command);
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
				horde.reset(ctx.getSource().getLevel());
				source.getEntity().sendSystemMessage(TextUtils.translatableComponent("commands.hordes.HordeReset.success", null, player.getName()));
				return 1;
			}
		}
		return 0;
	}
}