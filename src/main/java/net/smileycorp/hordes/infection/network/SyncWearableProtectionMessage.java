package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage implements IMessage {
	
	private final List<Tuple<Item, Integer>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Float> wearableProtection) {
		wearableProtection.forEach((item, chance) -> data.add(new Tuple(item, MathHelper.clamp((int)(100*(1-chance)), -100, 100))));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
			if (item != null) data.add(new Tuple<>(item, (int)buf.readByte()));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		data.forEach(e -> {
			ByteBufUtils.writeUTF8String(buf, ForgeRegistries.ITEMS.getKey(e.getFirst()).toString());
			buf.writeByte(e.getSecond());
		});
	}
	
	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> ClientInfectionEventHandler.readWearableProtection(data));
		return null;
	}

}