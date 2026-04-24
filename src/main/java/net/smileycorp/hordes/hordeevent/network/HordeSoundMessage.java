package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

public class HordeSoundMessage extends AbstractMessage {

	protected float dirX, dirZ;
	protected ResourceLocation sound;

	public HordeSoundMessage() {}

	public HordeSoundMessage(float dirX, float dirZ, ResourceLocation sound) {
		this.dirX = dirX;
		this.dirZ = dirZ;
		this.sound = sound;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		dirX = buf.readFloat();
		dirZ = buf.readFloat();
		sound = ResourceLocation.tryParse(buf.readUtf());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeFloat(dirX);
		buf.writeFloat(dirZ);
		if (sound != null) buf.writeUtf(sound.toString());
	}

	@Override
	public void handle(PacketListener handler) {}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> HordeClientHandler.INSTANCE.playHordeSound(dirX, dirZ, sound)));
		ctx.setPacketHandled(true);
	}

}
