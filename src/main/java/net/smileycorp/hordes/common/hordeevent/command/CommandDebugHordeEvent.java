package net.smileycorp.hordes.common.hordeevent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.hordeevent.capability.HordeSavedData;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommandDebugHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("debugHorde")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		HordeSavedData data = HordeSavedData.getData(source.getServer().overworld());
		if (!HordesLogger.logSaveData(data)) return 0;
		source.getEntity().sendSystemMessage(MutableComponent.create(new TranslatableContents("commands.hordes.HordeDebug.success",
				null, new Object[]{HordesLogger.getFiletext()})));
		return 1;
	}


}
