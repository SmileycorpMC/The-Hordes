package net.smileycorp.hordes.common.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import javax.annotation.Nullable;
import java.util.List;

public class SubCommandSpawnZombie implements HordesCommand.SubCommand {

    @Override
    public List<String> getTabCompletions(MinecraftServer server, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1)
                return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        if (args.length < 5)
                return CommandBase.getTabCompletionCoordinate(args, 0, pos);
        if (args.length == 5)
            return CommandBase.getListOfStringsMatchingLastWord(args, PlayerZombie.Type.names());
        return Lists.newArrayList();
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender sender, String[] args) throws CommandException {
        World level = sender.getEntityWorld();
        String player = args[0];
        BlockPos blockpos = CommandBase.parseBlockPos(sender, args, 1, false);
        PlayerZombie.Type<?> type = EntityZombiePlayer.TYPE;
        try {
            type = PlayerZombie.Type.get(args[5]);
        } catch (Exception e) {}
        if (type == null) throw new WrongUsageException("commands.hordes.spawn_zombie.usage");
        PlayerZombie<?> zombie = type.create(level);
        zombie.setPlayer(player);
        zombie.asEntity().setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        level.spawnEntity(zombie.asEntity());
    }

}
