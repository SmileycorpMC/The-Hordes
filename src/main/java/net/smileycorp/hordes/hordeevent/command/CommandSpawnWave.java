package net.smileycorp.hordes.hordeevent.command;

import com.google.common.collect.Lists;
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
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

import java.util.Collection;

public class CommandSpawnWave {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("spawnWave")
				.requires((commandSource) -> commandSource.hasPermission(1)).then(Commands.argument("count", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "count"), null))
						.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
								.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "count"), ResourceLocationArgument.getId(ctx, "table")))))
				.then(Commands.argument("player", EntityArgument.players()).then(Commands.argument("count", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "count"), EntityArgument.getPlayers(ctx, "player"), null))
						.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
								.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "count"),
										EntityArgument.getPlayers(ctx, "player"), ResourceLocationArgument.getId(ctx, "table")))))));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int count, ResourceLocation table) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof ServerPlayer) return execute(ctx, count, Lists.newArrayList(source.getPlayerOrException()), table);
		return 0;
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int count, Collection<ServerPlayer> players, ResourceLocation table) throws CommandSyntaxException {
		for (ServerPlayer player : players) {
			HordeEvent horde = HordeSavedData.getData(ctx.getSource().getLevel()).getEvent(player);
			try {
				HordeSpawnTable current = horde.getSpawnTable();
				horde.setSpawntable(HordeTableLoader.INSTANCE.getTable(table));
				horde.spawnWave(player, count);
				horde.setSpawntable(current);
			} catch (Exception e) {
				HordesLogger.logError("Failed to run startHordeEvent command", e);
			}
		}
		return 1;
	}

}
