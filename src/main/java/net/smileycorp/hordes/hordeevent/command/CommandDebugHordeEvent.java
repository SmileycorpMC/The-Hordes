package net.smileycorp.hordes.hordeevent.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;

public class CommandDebugHordeEvent {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
		command.then(Commands.literal("debug")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx)));
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		HordeSavedData data = HordeSavedData.getData(source.getServer().overworld());
		if (!HordesLogger.logSaveData(data)) return 0;
		source.getEntity().sendMessage(new TranslationTextComponent("commands.hordes.HordeDebug.success", HordesLogger.getFiletext()), null);
		return 1;
	}

}
