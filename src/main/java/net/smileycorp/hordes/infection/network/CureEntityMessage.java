package net.smileycorp.hordes.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.Constants;

public class CureEntityMessage implements NetworkMessage {
	
	public static Type<CureEntityMessage> TYPE = new Type(Constants.loc("cure_entity"));

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
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> ClientHandler.processCureEntity(this));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
}