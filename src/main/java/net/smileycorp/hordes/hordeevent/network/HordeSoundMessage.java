package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

public class HordeSoundMessage implements NetworkMessage {
	
	public static Type<HordeSoundMessage> TYPE = new Type(Constants.loc("horde_sound"));

	protected Vec3 direction;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vec3 direction, ResourceLocation sound) {
		this.direction = direction;
		this.sound = sound;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vec3(x, 0, z);
		sound = ResourceLocation.tryParse(buf.readUtf());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		if (direction != null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound != null) buf.writeUtf(sound.toString());
	}
	
	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> HordeClientHandler.INSTANCE.playHordeSound(direction, sound));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
