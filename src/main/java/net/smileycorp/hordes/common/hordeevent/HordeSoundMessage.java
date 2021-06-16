package net.smileycorp.hordes.common.hordeevent;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class HordeSoundMessage implements IMessage {
	
	Vec3d direction;
	
	public HordeSoundMessage() {}
	
	public HordeSoundMessage(Vec3d direction) {
		this.direction=direction;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		double x = buf.readDouble();
		double z = buf.readDouble();
		direction = new Vec3d(x, 0, z);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (direction!=null) {
			buf.writeDouble(direction.x);
			buf.writeDouble(direction.z);
		}
	}
	
	
	public Vec3d getDirection() {
		return direction;
	}
}
