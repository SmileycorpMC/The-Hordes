package net.smileycorp.hordes.common.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

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
			LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().reset(ctx.getSource().getLevel());
				source.getEntity().sendSystemMessage(MutableComponent.create(new TranslatableContents("commands.hordes.HordeReset.success", null, new Object[]{player.getName()})));
				return 1;
			}
		}
		return 0;
	}
}