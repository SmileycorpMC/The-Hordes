package net.smileycorp.hordes.infection.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.smileycorp.hordes.client.ClientHandler;

public class InfectMessage implements IPacket<INetHandler> {


	private boolean prevented;

	public InfectMessage() {}

	public InfectMessage(boolean prevented) {
		this.prevented = prevented;
	}

	@Override
	public void read(PacketBuffer buf) {
		prevented = buf.readBoolean();
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeBoolean(prevented);
	}

	@Override
	public void handle(INetHandler handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.onInfect(prevented)));
		ctx.setPacketHandled(true);
	}

}