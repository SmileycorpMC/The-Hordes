package net.smileycorp.hordes.common.hordeevent.command;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.smileycorp.hordes.common.hordeevent.capability.HordeWorldData;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandDebugHordeEvent {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("debugHorde")
				.requires((commandSource) -> commandSource.hasPermission(1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		Path path = Paths.get("logs/hordes.log");
		HordeWorldData data = HordeWorldData.getData(source.getServer().overworld());
		List<String> out = data.getDebugText();
		try {
			Files.write(path, out, StandardCharsets.UTF_8);
		} catch (Exception e) {}
		source.getEntity().sendMessage(new TranslationTextComponent("commands.hordes.HordeDebug.success", path.toAbsolutePath().toString()), UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb"));
		return 1;
	}

}
