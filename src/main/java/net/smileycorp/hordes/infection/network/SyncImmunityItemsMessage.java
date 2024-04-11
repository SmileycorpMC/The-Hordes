package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class SyncImmunityItemsMessage extends AbstractMessage {
	
	private final List<Map.Entry<Item, Integer>> data = Lists.newArrayList();
	
	public SyncImmunityItemsMessage() {}

	public SyncImmunityItemsMessage(Map<Item, Integer> immunityItems) {
		data.addAll(immunityItems.entrySet());
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf()));
			if (item != null) data.add(new AbstractMap.SimpleEntry(item, buf.readInt()));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		data.forEach(e -> {
			buf.writeUtf(ForgeRegistries.ITEMS.getKey(e.getKey()).toString());
			buf.writeInt(e.getValue());
		});
	}

	@Override
	public void handle(PacketListener handler) {}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientInfectionEventHandler.readImmunityItems(data)));
		ctx.setPacketHandled(true);
	}

}