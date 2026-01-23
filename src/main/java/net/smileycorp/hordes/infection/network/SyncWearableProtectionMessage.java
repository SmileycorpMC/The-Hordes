package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.infection.data.InfectionData;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage implements NetworkMessage {
	
	public static Type<SyncWearableProtectionMessage> TYPE = new Type(Constants.loc("sync_wearable_protection"));
	
	private final List<Pair<Item, Pair<Float, AttributeModifier.Operation>>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Pair<Float, AttributeModifier.Operation>> wearableProtection) {
		wearableProtection.forEach((item, pair) -> data.add(Pair.of(item, pair)));
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		while (buf.isReadable()) {
			Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(buf.readUtf()));
			if (item != null) data.add(Pair.of(item, Pair.of(buf.readFloat(), AttributeModifier.Operation.BY_ID.apply(buf.readByte()))));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		data.forEach(e -> {
			buf.writeUtf(BuiltInRegistries.ITEM.getKey(e.getFirst()).toString());
			Pair<Float, AttributeModifier.Operation> pair = e.getSecond();
			buf.writeFloat(pair.getFirst());
			buf.writeByte(pair.getSecond().ordinal());
		});
	}

	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> InfectionData.INSTANCE.readWearableProtection(data));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
}