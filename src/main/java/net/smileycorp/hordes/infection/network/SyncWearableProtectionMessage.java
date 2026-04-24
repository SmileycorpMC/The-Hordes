package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.hordes.infection.client.InfectionClientHandler;
import net.smileycorp.hordes.infection.data.InfectionData;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage extends AbstractMessage {

	private final List<Pair<Item, Pair<Float, AttributeModifier.Operation>>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Pair<Float, AttributeModifier.Operation>> wearableProtection) {
		wearableProtection.forEach((item, pair) -> data.add(Pair.of(item, pair)));
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		while (buf.isReadable()) {
			Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(buf.readUtf()));
			if (item != null) data.add(Pair.of(item, Pair.of(buf.readFloat(), AttributeModifier.Operation.fromValue(buf.readByte()))));
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
	public void handle(PacketListener handler) {}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> InfectionData.INSTANCE.readWearableProtection(data)));
		ctx.setPacketHandled(true);
	}

}