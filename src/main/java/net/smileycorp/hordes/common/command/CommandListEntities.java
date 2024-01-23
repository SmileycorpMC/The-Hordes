package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.HordesLogger;

public class CommandListEntities {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("listEntities")
				.requires((commandSource) -> commandSource.hasPermission(-1))
				.executes(ctx -> execute(ctx)));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		for (ResourceLocation loc : ForgeRegistries.ENTITY_TYPES.getKeys()) {
			HordesLogger.logSilently(loc + " - " + ForgeRegistries.ENTITY_TYPES.getValue(loc).toShortString());
		}
		ctx.getSource().getEntity().sendSystemMessage(TextUtils.translatableComponent("commands.hordes.ListEntities.success", null, HordesLogger.getFiletext()));
		return 1;
	}

}
