package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandSpawnWave {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("spawnHordeWave")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.then(Commands.argument("count", IntegerArgumentType.integer()))
				.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "count")));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSource> ctx, int count) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		if (source.getEntity() instanceof Entity) {
			PlayerEntity player = (PlayerEntity) source.getEntity();
			LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().spawnWave(player, count);
				return 1;
			}
		}
		return 0;
	}

}
