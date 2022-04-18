package net.smileycorp.hordes.common.hordeevent.command;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.smileycorp.hordes.common.hordeevent.capability.HordeLevelData;

public class CommandDebugHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("debugHorde")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		Path path = Paths.get("logs/hordes.log");
		HordeLevelData data = HordeLevelData.getData(source.getServer().overworld());
		List<String> out = data.getDebugText();
		try {
			Files.write(path, out, StandardCharsets.UTF_8);
		} catch (Exception e) {}
		source.getEntity().sendMessage(new TranslatableComponent("commands.hordes.HordeDebug.success", path.toAbsolutePath().toString()), UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb"));
		return 1;
	}

}
