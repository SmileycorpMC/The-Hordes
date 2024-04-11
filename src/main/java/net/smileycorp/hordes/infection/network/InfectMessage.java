package net.smileycorp.hordes.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;

public class InfectMessage extends SimpleAbstractMessage {


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
	public void handle(PacketListener handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.onInfect(prevented)));
		ctx.setPacketHandled(true);
	}

}