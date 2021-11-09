package net.smileycorp.hordes.infection;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CureEntityMessage implements IMessage {

	private int entity;

	public CureEntityMessage() {}

	public CureEntityMessage(Entity entity) {
		this.entity = entity.getEntityId();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entity);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entity = buf.readInt();
	}

	public Entity getEntity(World world) {
		return world.getEntityByID(entity);
	}
}