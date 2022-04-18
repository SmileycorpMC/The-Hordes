package net.smileycorp.hordes.common.hordeevent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

public class CommandStartHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("startHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.then(Commands.argument("length", IntegerArgumentType.integer()))
				.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "length")));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx, int length) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof Entity) {
			Player player = (Player) source.getEntity();
			LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().tryStartEvent(player, length, true);
				return 1;
			}
		}
		return 0;
	}

}
