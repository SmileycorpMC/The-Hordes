package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.infection.client.InfectionClientHandler;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class SyncImmunityItemsMessage implements NetworkMessage {
	
	public static Type<SyncImmunityItemsMessage> TYPE = new Type(Constants.loc("sync_immunity_items"));
	
	private final List<Map.Entry<Item, Integer>> data = Lists.newArrayList();
	
	public SyncImmunityItemsMessage() {}

	public SyncImmunityItemsMessage(Map<Item, Integer> immunityItems) {
		data.addAll(immunityItems.entrySet());
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		while (buf.isReadable()) {
			Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(buf.readUtf()));
			if (item != null) data.add(new AbstractMap.SimpleEntry(item, buf.readInt()));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		data.forEach(e -> {
			buf.writeUtf(BuiltInRegistries.ITEM.getKey(e.getKey()).toString());
			buf.writeInt(e.getValue());
		});
	}

	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> InfectionClientHandler.INSTANCE.readImmunityItems(data));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
}