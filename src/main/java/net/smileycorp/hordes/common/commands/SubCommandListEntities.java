package net.smileycorp.hordes.common.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.smileycorp.hordes.common.HordesLogger;

public class SubCommandListEntities implements HordesCommand.SubCommand {

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] args) throws CommandException {
        ForgeRegistries.ENTITIES.getEntries().forEach(entry -> HordesLogger.logSilently(entry.getKey() + " - " + entry.getValue().getEntityClass()));
        Entity entity = iCommandSender.getCommandSenderEntity();
        if (entity == null) return;
        entity.sendMessage(new TextComponentTranslation("commands.hordes.ListEntities.success", HordesLogger.getFiletext()));
    }

}
