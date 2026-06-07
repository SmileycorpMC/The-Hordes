package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.data.Pair;
import net.smileycorp.hordes.config.data.infection.InfectionData;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage implements IMessage {

	private final List<Pair<Item, Pair<Float, Byte>>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Pair<Float, Byte>> wearableProtection) {
		wearableProtection.forEach((item, pair) -> data.add(Pair.of(item, pair)));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
			if (item != null) data.add(Pair.of(item, Pair.of(buf.readFloat(), buf.readByte())));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		data.forEach(e -> {
			ByteBufUtils.writeUTF8String(buf, ForgeRegistries.ITEMS.getKey(e.getFirst()).toString());
			Pair<Float, Byte> pair = e.getSecond();
			buf.writeFloat(pair.getFirst());
			buf.writeByte(pair.getSecond());
		});
	}
	
	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> InfectionData.INSTANCE.readWearableProtection(data));
		return null;
	}

}