package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.HordesEntities;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;

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
