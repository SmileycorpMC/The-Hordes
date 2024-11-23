package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

public class HordeSoundMessage implements NetworkMessage {
	
	public static Type<HordeSoundMessage> TYPE = new Type(Constants.loc("horde_sound"));

	protected float dirX, dirZ;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(float dirX, float dirZ, ResourceLocation sound) {
		this.dirX = dirX;
		this.dirZ = dirZ;
		this.sound = sound;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		dirX = buf.readFloat();
		dirZ = buf.readFloat();
		sound = ResourceLocation.tryParse(buf.readUtf());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeFloat(dirX);
		buf.writeFloat(dirZ);
		if (sound != null) buf.writeUtf(sound.toString());
	}
	
	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> HordeClientHandler.INSTANCE.playHordeSound(dirX, dirZ, sound));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
