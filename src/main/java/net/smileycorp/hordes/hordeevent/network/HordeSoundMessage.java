package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.smileycorp.hordes.client.ClientHandler;

public class HordeSoundMessage implements IPacket<INetHandler> {

	protected Vector3d direction;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vector3d direction, ResourceLocation sound) {
		this.direction = direction;
		this.sound = sound;
	}

	@Override
	public void read(PacketBuffer buf) {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vector3d(x, 0, z);
		sound = new ResourceLocation(buf.readUtf());
	}

	@Override
	public void write(PacketBuffer buf) {
		if (direction != null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound != null) buf.writeUtf(sound.toString());
	}

	@Override
	public void handle(INetHandler handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.playHordeSound(direction, sound)));
		ctx.setPacketHandled(true);
	}

}
