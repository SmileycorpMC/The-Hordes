package net.smileycorp.hordes.common.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.ConfigDataManager;

import java.time.LocalTime;

public class SubCommandReload implements HordesCommand.SubCommand {

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] args) throws CommandException {
        ConfigDataManager.reload();
        Entity entity = iCommandSender.getCommandSenderEntity();
        if (entity == null) return;
        entity.sendMessage(new TextComponentTranslation("commands.hordes.Reload.success", "[" + LocalTime.now() + "]"));
    }

}
