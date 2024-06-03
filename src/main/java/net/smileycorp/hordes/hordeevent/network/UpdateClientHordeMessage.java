package net.smileycorp.hordes.hordeevent.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.smileycorp.hordes.client.ClientHandler;

public class UpdateClientHordeMessage implements IMessage {
	
	private int day, day_length;
	
	public UpdateClientHordeMessage() {}
	
	public UpdateClientHordeMessage(int day) {
		this.day = day;
	}
	
	public UpdateClientHordeMessage(int day, int day_length) {
		this.day = day;
		this.day_length = day_length;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		day = buf.readInt();
		day_length = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(day);
		buf.writeInt(day_length);
	}
	
	public void process() {
		ClientHandler.setHordeDay(day, day_length);
	}
	
}
