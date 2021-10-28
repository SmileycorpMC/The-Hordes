package net.smileycorp.hordes.common.hordeevent;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class HordeSoundMessage implements IMessage {

	protected Vec3d direction;
	protected SoundEvent sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vec3d direction, SoundEvent sound) {
		this.direction=direction;
		this.sound=sound;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vec3d(x, 0, z);
		sound = new SoundEvent(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (direction!=null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound!=null) {
			ByteBufUtils.writeUTF8String(buf, sound.getRegistryName().toString());
		}
	}


	public Vec3d getDirection() {
		return direction;
	}

	public SoundEvent getSound() {
		return sound;
	}
}
