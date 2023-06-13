package net.smileycorp.hordes.common.hordeevent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeEvent;

public class CommandStopHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> comamnd = Commands.literal("stopHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(comamnd);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		if (source.getEntity() instanceof Player) {
			Player player = (Player) source.getEntity();
			LazyOptional<IHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().stopEvent(player, true);
				return 1;
			}
		}
		return 0;
	}
}
