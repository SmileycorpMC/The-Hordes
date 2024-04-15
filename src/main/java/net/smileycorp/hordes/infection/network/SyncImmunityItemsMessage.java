package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class SyncImmunityItemsMessage implements IPacket<INetHandler> {
	
	private final List<Map.Entry<Item, Integer>> data = Lists.newArrayList();
	
	public SyncImmunityItemsMessage() {}

	public SyncImmunityItemsMessage(Map<Item, Integer> immunityItems) {
		data.addAll(immunityItems.entrySet());
	}

	@Override
	public void read(PacketBuffer buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf()));
			if (item != null) data.add(new AbstractMap.SimpleEntry(item, buf.readInt()));
		}
	}

	@Override
	public void write(PacketBuffer buf) {
		data.forEach(e -> {
			buf.writeUtf(ForgeRegistries.ITEMS.getKey(e.getKey()).toString());
			buf.writeInt(e.getValue());
		});
	}

	@Override
	public void handle(INetHandler handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientInfectionEventHandler.readImmunityItems(data)));
		ctx.setPacketHandled(true);
	}

}