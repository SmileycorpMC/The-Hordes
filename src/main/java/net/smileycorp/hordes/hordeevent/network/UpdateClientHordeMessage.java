package net.smileycorp.hordes.hordeevent.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

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

	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> HordeClientHandler.INSTANCE.setHordeDay(horde_day, day_length));
		return null;
	}
	
}
