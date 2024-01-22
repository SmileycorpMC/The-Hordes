package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.command.EnumArgument;
import net.smileycorp.hordes.common.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class CommandSpawnZombie {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("spawnZombie")
				.requires((commandSource) -> commandSource.hasPermission(1)).then(Commands.argument("username", StringArgumentType.string())
						.then(Commands.argument("pos", Vec3Argument.vec3()).executes(CommandSpawnZombie::execute)
								.then(Commands.argument("drowned", EnumArgument.enumArgument(Type.class)).executes(CommandSpawnZombie::execute)))));
	}

	public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		String player = StringArgumentType.getString(ctx, "username");
		Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
		Type type = Type.ZOMBIE;
		try {
			type = ctx.getArgument("type", Type.class);
		} catch (Exception e) {}
		PlayerZombie zombie = type.create(level);
		zombie.setPlayer(player);
		zombie.asEntity().setPos(pos);
		level.addFreshEntity(zombie.asEntity());
		return 1;
	}

	private enum Type {
		ZOMBIE(HordesEntities.ZOMBIE_PLAYER.get()),
		DROWNED(HordesEntities.DROWNED_PLAYER.get()),
		HUSK(HordesEntities.HUSK_PLAYER.get());

		private final EntityType<? extends PlayerZombie> type;

		Type(EntityType<? extends PlayerZombie> type) {
			this.type = type;
		}

		private PlayerZombie create(ServerLevel level) {
			return type.create(level);
		}

	}

}
