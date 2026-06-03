package net.smileycorp.hordes.hordeevent.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

public class HordeSoundMessage implements IMessage {

	protected float dirX, dirZ;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(float dirX, float dirZ, ResourceLocation sound) {
		this.dirX = dirX;
		this.dirZ = dirZ;
		this.sound = sound;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dirX = buf.readFloat();
		dirZ = buf.readFloat();
		sound = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(dirX);
		buf.writeFloat(dirZ);
		if (sound != null) ByteBufUtils.writeUTF8String(buf, sound.toString());
	}

	public IMessage process(MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> HordeClientHandler.INSTANCE.playHordeSound(dirX, dirZ, sound));
		return null;
	}

}
