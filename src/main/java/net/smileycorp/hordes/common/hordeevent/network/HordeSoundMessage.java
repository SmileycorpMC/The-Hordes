package net.smileycorp.hordes.common.hordeevent.network;

import java.io.IOException;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class HordeSoundMessage implements IPacket<INetHandler> {

	protected Vector3d direction;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vector3d direction, ResourceLocation sound) {
		this.direction=direction;
		this.sound=sound;
	}

	@Override
	public void read(PacketBuffer buf) throws IOException {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vector3d(x, 0, z);
		sound = new ResourceLocation(buf.readUtf());
	}

	@Override
	public void write(PacketBuffer buf) throws IOException {
		if (direction!=null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound!=null) {
			buf.writeUtf(sound.toString());
		}
	}


	public Vector3d getDirection() {
		return direction;
	}

	public ResourceLocation getSound() {
		return sound;
	}

	@Override
	public void handle(INetHandler handler) {}

}
