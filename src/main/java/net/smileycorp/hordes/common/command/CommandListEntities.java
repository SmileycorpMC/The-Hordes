package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;

public class CommandListEntities {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
		command.then(Commands.literal("listEntities")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx)));
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ForgeRegistries.ENTITIES.getKeys().forEach(loc -> HordesLogger.logSilently(loc + " - " + ForgeRegistries.ENTITIES.getValue(loc).toShortString()));
		ctx.getSource().getEntity().sendMessage(new TranslationTextComponent("commands.hordes.ListEntities.success", HordesLogger.getFiletext()), null);
		return 1;
	}

}
