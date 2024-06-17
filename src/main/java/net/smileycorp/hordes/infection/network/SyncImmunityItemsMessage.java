package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class SyncImmunityItemsMessage implements IMessage {
	
	private final List<Map.Entry<ItemStack, Integer>> data = Lists.newArrayList();
	
	public SyncImmunityItemsMessage() {}

	public SyncImmunityItemsMessage(Map<ItemStack, Integer> immunityItems) {
		data.addAll(immunityItems.entrySet());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		while (buf.isReadable()) {
			String str = ByteBufUtils.readUTF8String(buf);
			try {
				ItemStack item = new ItemStack(JsonToNBT.getTagFromJson(str));
				if (item != null) data.add(new AbstractMap.SimpleEntry(item, buf.readInt()));
			} catch (Exception e) {
				HordesLogger.logError("Failed reading immunity item " + str + " from network", e);
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		data.forEach(e -> {
			ByteBufUtils.writeUTF8String(buf, e.getKey().writeToNBT(new NBTTagCompound()).toString());
			buf.writeInt(e.getValue());
		});
	}
	
	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> ClientInfectionEventHandler.readImmunityItems(data));
		return null;
	}

}