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
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;

import java.util.Collection;

public class CommandStartHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("startHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1)).then(Commands.argument("length", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length")))
				.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
				.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "length"), ResourceLocationArgument.getId(ctx, "table")))))
				.then(Commands.argument("player", EntityArgument.players()).then(Commands.argument("length", IntegerArgumentType.integer())
				.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length"), EntityArgument.getPlayers(ctx, "player")))
				.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
				.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "length"),
						EntityArgument.getPlayers(ctx, "player"), ResourceLocationArgument.getId(ctx, "table"))))));
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
			try {
				if (optional.isPresent()) {
					optional.resolve().get().tryStartEvent(player, length, true);
				}
			} catch (Exception e) {
				Hordes.logError("Failed to run startHordeEvent command", e);
			}
		}
		return 1;
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int length, ResourceLocation table) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof Player) return execute(ctx, length, Lists.newArrayList(source.getPlayerOrException()), table);
		return 0;
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int length, Collection<ServerPlayer> players, ResourceLocation table) throws CommandSyntaxException {
		for (Player player : players) {
			LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			try {
				if (optional.isPresent()) {
					IHordeEvent event = optional.resolve().get();
					event.setSpawntable(HordeTableLoader.INSTANCE.getTable(table));
					event.tryStartEvent(player, length, true);
				}
			} catch (Exception e) {
				Hordes.logError("Failed to run startHordeEvent command", e);
			}
		}
		return 1;
	}

}
