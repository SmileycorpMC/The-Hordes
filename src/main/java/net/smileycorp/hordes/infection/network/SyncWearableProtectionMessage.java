package net.smileycorp.hordes.infection.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;

import java.util.List;
import java.util.Map;

public class SyncWearableProtectionMessage implements IPacket<INetHandler> {
	
	private final List<Pair<Item, Integer>> data = Lists.newArrayList();
	
	public SyncWearableProtectionMessage() {}

	public SyncWearableProtectionMessage(Map<Item, Float> wearableProtection) {
		wearableProtection.forEach((item, chance) -> data.add(Pair.of(item, MathHelper.clamp((int)(100*(1-chance)), -100, 100))));
	}

	@Override
	public void read(PacketBuffer buf) {
		while (buf.isReadable()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(buf.readUtf()));
			if (item != null) data.add(Pair.of(item, (int)buf.readByte()));
		}
	}

	@Override
	public void write(PacketBuffer buf) {
		data.forEach(e -> {
			buf.writeUtf(ForgeRegistries.ITEMS.getKey(e.getFirst()).toString());
			buf.writeByte(e.getSecond());
		});
	}

	@Override
	public void handle(INetHandler handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientInfectionEventHandler.readWearableProtection(data)));
		ctx.setPacketHandled(true);
	}

}