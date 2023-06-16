package net.smileycorp.hordes.common.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;

public class CureEntityMessage extends SimpleAbstractMessage {

	private int entity;

	public CureEntityMessage() {}

	public CureEntityMessage(LivingEntity entity) {
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

	public LivingEntity getEntity(Level level) {
		return (LivingEntity) level.getEntity(entity);
	}

	@Override
	public void handle(PacketListener handler) {}

}