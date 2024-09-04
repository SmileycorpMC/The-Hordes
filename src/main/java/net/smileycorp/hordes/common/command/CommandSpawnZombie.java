package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.command.EnumArgument;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class CommandSpawnZombie {

	public static void register(LiteralArgumentBuilder<CommandSource> command) {
		command.then(Commands.literal("spawnZombie")
				.requires((commandSource) -> commandSource.hasPermission(1)).then(Commands.argument("username", StringArgumentType.string())
						.then(Commands.argument("pos", Vec3Argument.vec3()).executes(CommandSpawnZombie::execute)
								.then(Commands.argument("type", EnumArgument.enumArgument(Type.class)).executes(CommandSpawnZombie::execute)))));
	}

	public static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ServerWorld level = ctx.getSource().getLevel();
		String player = StringArgumentType.getString(ctx, "username");
		Vector3d pos = Vec3Argument.getVec3(ctx, "pos");
		Type type = Type.ZOMBIE;
		try {
			type = ctx.getArgument("type", Type.class);
		} catch (Exception e) {}
		PlayerZombie zombie = type.create(level);
		zombie.setPlayer(player);
		zombie.asEntity().setPos(pos.x, pos.y, pos.z);
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

		private PlayerZombie create(ServerWorld level) {
			return type.create(level);
		}

	}

}
