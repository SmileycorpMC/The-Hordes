package net.smileycorp.hordes.hordeevent.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.HordeSpawnTable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

import java.util.Collection;

public class CommandSpawnWave {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
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

	public static int execute(CommandContext<CommandSource> ctx, int count, ResourceLocation table) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		if (source.getEntity() instanceof ServerPlayerEntity) return execute(ctx, count, Lists.newArrayList(source.getPlayerOrException()), table);
		return 0;
	}

	public static int execute(CommandContext<CommandSource> ctx, int count, Collection<ServerPlayerEntity> players, ResourceLocation table) throws CommandSyntaxException {
		for (ServerPlayerEntity player : players) {
			HordeEvent horde = HordeSavedData.getData(ctx.getSource().getLevel()).getEvent(player);
			try {
				HordeSpawnTable current = horde.getSpawntable();
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
