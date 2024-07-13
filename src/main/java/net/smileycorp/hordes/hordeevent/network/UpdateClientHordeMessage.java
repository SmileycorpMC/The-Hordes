package net.smileycorp.hordes.hordeevent.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.config.HordeEventConfig;

public class UpdateClientHordeMessage implements IMessage {
	
	private boolean horde_day;
	private int day_length;
	
	public UpdateClientHordeMessage() {}
	
	public UpdateClientHordeMessage(boolean horde_day) {
		this.horde_day = horde_day;
		this.day_length = HordeEventConfig.dayLength;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		horde_day = buf.readBoolean();
		day_length = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(horde_day);
		buf.writeInt(day_length);
	}
	
	public void process() {
		ClientHandler.setHordeDay(horde_day, day_length);
	}
	
}
