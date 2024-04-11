package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;

public class HordeSoundMessage extends SimpleAbstractMessage {

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
		sound = new ResourceLocation(buf.readUtf());
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
	public void handle(PacketListener handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.playHordeSound(direction, sound)));
		ctx.setPacketHandled(true);
	}

}
