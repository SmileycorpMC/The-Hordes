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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

import java.util.Collection;

public class CommandStartHordeEvent {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
		command.then(Commands.literal("start")
				.requires((commandSource) -> commandSource.hasPermission(1)).then(Commands.argument("length", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length"), null))
				.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
				.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "length"), ResourceLocationArgument.getId(ctx, "table")))))
				.then(Commands.argument("player", EntityArgument.players()).then(Commands.argument("length", IntegerArgumentType.integer())
				.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length"), EntityArgument.getPlayers(ctx, "player"), null))
				.then(Commands.argument("table", ResourceLocationArgument.id()).suggests(HordeTableLoader::getSuggestions)
				.executes(ctx -> execute(ctx,IntegerArgumentType.getInteger(ctx, "length"),
						EntityArgument.getPlayers(ctx, "player"), ResourceLocationArgument.getId(ctx, "table")))))));
	}

	public static int execute(CommandContext<CommandSource> ctx, int length, ResourceLocation table) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		if (source.getEntity() instanceof PlayerEntity) return execute(ctx, length, Lists.newArrayList(source.getPlayerOrException()), table);
		return 0;
	}

	public static int execute(CommandContext<CommandSource> ctx, int length, Collection<ServerPlayerEntity> players, ResourceLocation table) throws CommandSyntaxException {
		for (ServerPlayerEntity player : players) {
			HordeEvent horde = HordeSavedData.getData(ctx.getSource().getLevel()).getEvent(player);
			try {
				if (table != null) horde.setSpawntable(HordeTableLoader.INSTANCE.getTable(table));
				horde.tryStartEvent(player, length, true);
			} catch (Exception e) {
				HordesLogger.logError("Failed to run startHordeEvent command", e);
			}
		}
		return 1;
	}

}
