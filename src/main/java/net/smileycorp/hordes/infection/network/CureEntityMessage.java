package net.smileycorp.hordes.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;

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
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processCureEntity(this)));
		ctx.setPacketHandled(true);
	}

}