package net.smileycorp.hordes.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.Constants;

public class InfectMessage implements NetworkMessage {
	
	public static CustomPacketPayload.Type<InfectMessage> TYPE = new CustomPacketPayload.Type(Constants.loc("infect"));

	private boolean prevented;

	public InfectMessage() {}

	public InfectMessage(boolean prevented) {
		this.prevented = prevented;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		prevented = buf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(prevented);
	}

	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> ClientHandler.onInfect(prevented));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
}