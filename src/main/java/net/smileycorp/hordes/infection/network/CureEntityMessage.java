package net.smileycorp.hordes.infection.network;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.smileycorp.hordes.client.ClientHandler;

public class CureEntityMessage implements IPacket<INetHandler> {

	private int entity;

	public CureEntityMessage() {}

	public CureEntityMessage(LivingEntity entity) {
		this.entity = entity.getId();
	}

	@Override
	public void read(PacketBuffer buf) {
		entity = buf.readInt();
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeInt(entity);
	}

	public LivingEntity getEntity(World level) {
		return (LivingEntity) level.getEntity(entity);
	}

	@Override
	public void handle(INetHandler handler) {}
	
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processCureEntity(this)));
		ctx.setPacketHandled(true);
	}

}