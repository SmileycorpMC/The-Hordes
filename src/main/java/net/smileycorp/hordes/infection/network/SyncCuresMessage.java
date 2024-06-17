package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.List;

public class SyncCuresMessage implements IMessage {
	
	private final List<ItemStack> data = Lists.newArrayList();
	
	public SyncCuresMessage() {}

	public SyncCuresMessage(List<ItemStack> immunityItems) {
		data.addAll(immunityItems);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		while (buf.isReadable()) {
			String str = ByteBufUtils.readUTF8String(buf);
			try {
				data.add(new ItemStack(JsonToNBT.getTagFromJson(str)));
			} catch (NBTException e) {
				HordesLogger.logError("Failed reading cure item " + str + " from network", e);
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		data.forEach(e -> ByteBufUtils.writeUTF8String(buf, e.writeToNBT(new NBTTagCompound()).toString()));
	}
	
	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> ClientInfectionEventHandler.readCures(data));
		return null;
	}

}