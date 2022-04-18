package net.smileycorp.hordes.common.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;

public class HordeSoundMessage extends SimpleAbstractMessage {

	protected Vec3 direction;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(Vec3 direction, ResourceLocation sound) {
		this.direction=direction;
		this.sound=sound;
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
		if (direction!=null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
		if (sound!=null) {
			buf.writeUtf(sound.toString());
		}
	}


	public Vec3 getDirection() {
		return direction;
	}

	public ResourceLocation getSound() {
		return sound;
	}

	@Override
	public void handle(PacketListener handler) {}

}
