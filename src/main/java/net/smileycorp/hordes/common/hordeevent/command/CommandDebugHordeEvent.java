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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.smileycorp.hordes.common.hordeevent.capability.HordeSavedData;

public class CommandDebugHordeEvent {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("debugHorde")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx));
		dispatcher.register(command);
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		Path path = Paths.get("logs/hordes.log");
		HordeSavedData data = HordeSavedData.getData(source.getServer().overworld());
		List<String> out = data.getDebugText();
		try {
			Files.write(path, out, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return 0;
		}
		String file = path.toAbsolutePath().toString();
		TextComponent text = new TextComponent(file);
		text.setStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(file))));
		source.getEntity().sendMessage(new TranslatableComponent("commands.hordes.HordeDebug.success", text), UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb"));
		return 1;
	}

}
