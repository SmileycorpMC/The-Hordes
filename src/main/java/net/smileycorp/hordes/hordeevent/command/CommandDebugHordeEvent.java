package net.smileycorp.hordes.hordeevent.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;

public class CommandDebugHordeEvent {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("debug")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx)));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		HordeSavedData data = HordeSavedData.getData(source.getServer().overworld());
		if (!HordesLogger.logSaveData(data)) return 0;
		source.getEntity().sendMessage(new TranslatableComponent("commands.hordes.HordeDebug.success", HordesLogger.getFiletext()), null);
		return 1;
	}

}
