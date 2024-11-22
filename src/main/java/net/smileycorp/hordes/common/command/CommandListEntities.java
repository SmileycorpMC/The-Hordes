package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.smileycorp.hordes.common.HordesLogger;

public class CommandListEntities {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("listEntities").requires((commandSource) -> commandSource.hasPermission(-1)).executes(CommandListEntities::execute));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) {
		BuiltInRegistries.ENTITY_TYPE.keySet().forEach(loc -> HordesLogger.logSilently(loc + " - " + BuiltInRegistries.ENTITY_TYPE.get(loc).toShortString()));
		ctx.getSource().getEntity().sendSystemMessage(Component.translatable("commands.hordes.ListEntities.success",HordesLogger.getFiletext()));
		return 1;
	}

}
