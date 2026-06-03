package net.smileycorp.hordes.common.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class HordesCommand extends CommandBase {

    private Map<String, SubCommand> sub_commands = Maps.newHashMap();

    @Override
    public String getName() {
        return "hordes";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "hordes <subcommand>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length <= 1) return Lists.newArrayList(sub_commands.keySet());
        if (sub_commands.containsKey(args[0])) sub_commands.get(args[0]).getTabCompletions(server, ArrayUtils.remove(args, 0), pos);
        return Lists.newArrayList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) throw new CommandNotFoundException();
        if (sub_commands.containsKey(args[0])) {
            sub_commands.get(args[0]).execute(server, sender, ArrayUtils.remove(args, 0));
            return;
        }
        throw new CommandNotFoundException();
    }

    public void register(String name, SubCommand subcommand) {
        sub_commands.put(name, subcommand);
    }

    public interface SubCommand {

        void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] args) throws CommandException;

        default List<String> getTabCompletions(MinecraftServer server, String[] args, BlockPos pos) {
            return Lists.newArrayList();
        }

    }

}
