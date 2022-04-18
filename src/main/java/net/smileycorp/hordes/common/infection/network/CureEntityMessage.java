package net.smileycorp.hordes.common.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;


public class CureEntityMessage extends SimpleAbstractMessage {

	private int entity;

	public CureEntityMessage() {}

	public CureEntityMessage(Mob entity) {
		this.entity = entity.getId();
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		entity = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entity);
	}

	public Mob getEntity(Level level) {
		return (Mob) level.getEntity(entity);
	}

	@Override
	public void handle(PacketListener handler) {}

}