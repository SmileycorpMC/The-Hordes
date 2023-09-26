package net.smileycorp.hordes.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;

public class InfectMessage extends AbstractMessage {

	public InfectMessage(){}

	@Override
	public void read(FriendlyByteBuf buf) {}

	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public void handle(PacketListener handler) {}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.onInfect()));
		ctx.setPacketHandled(true);
	}

}