package net.smileycorp.hordes.hordeevent.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class HordeSoundMessage implements IMessage {

	protected Vec3d direction;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vec3d direction, ResourceLocation sound) {
		this.direction=direction;
		this.sound=sound;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vec3d(x, 0, z);
		sound = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (direction!=null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound!=null) {
			ByteBufUtils.writeUTF8String(buf, sound.toString());
		}
	}


	public Vec3d getDirection() {
		return direction;
	}

	public ResourceLocation getSound() {
		return sound;
	}
}
