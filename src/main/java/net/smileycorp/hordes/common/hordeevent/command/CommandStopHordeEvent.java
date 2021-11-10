package net.smileycorp.hordes.common.hordeevent.command;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandStopHordeEvent {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> comamnd = Commands.literal("stopHordeEvent")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(comamnd);
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		if (source.getEntity() instanceof Entity) {
			PlayerEntity player = (PlayerEntity) source.getEntity();
			LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
			if (optional.isPresent()) {
				optional.resolve().get().stopEvent(source.getServer().overworld(), true);
				return 1;
			}
		}
		return 0;
	}
}
