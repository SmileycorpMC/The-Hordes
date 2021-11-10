package net.smileycorp.hordes.common.infection.network;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class CureEntityMessage implements IPacket<INetHandler> {

	private int entity;

	public CureEntityMessage() {}

	public CureEntityMessage(Entity entity) {
		this.entity = entity.getId();
	}

	@Override
	public void read(PacketBuffer buf) throws IOException {
		entity = buf.readInt();
	}

	@Override
	public void write(PacketBuffer buf) throws IOException {
		buf.writeInt(entity);
	}

	public MobEntity getEntity(World world) {
		return (MobEntity) world.getEntity(entity);
	}

	@Override
	public void handle(INetHandler handler) {}

}