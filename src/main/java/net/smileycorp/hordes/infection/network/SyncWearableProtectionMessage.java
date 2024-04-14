package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage extends SimpleAbstractMessage {
	
	private final List<Pair<Item, Integer>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Float> wearableProtection) {
		wearableProtection.forEach((item, chance) -> data.add(Pair.of(item, Mth.clamp((int)(100*(1-chance)), -100, 100))));
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf()));
			if (item != null) data.add(Pair.of(item, (int)buf.readByte()));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		data.forEach(e -> {
			buf.writeUtf(ForgeRegistries.ITEMS.getKey(e.getFirst()).toString());
			buf.writeByte(e.getSecond());
		});
	}

	@Override
	public void handle(PacketListener handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientInfectionEventHandler.readWearableProtection(data)));
		ctx.setPacketHandled(true);
	}

}