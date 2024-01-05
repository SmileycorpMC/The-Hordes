package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;

public class UpdateClientHordeMessage extends AbstractMessage {

    private int day, day_length;

    public UpdateClientHordeMessage() {}

    public UpdateClientHordeMessage(int day) {
        this.day = day;
    }

    public UpdateClientHordeMessage(int day, int day_length) {
        this.day = day;
        this.day_length = day_length;
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        day = buf.readInt();
        day_length = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(day);
        buf.writeInt(day_length);
    }

    @Override
    public void process(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setHordeDay(day, day_length)));
        ctx.setPacketHandled(true);
    }

    @Override
    public void handle(PacketListener p_131342_) {}

}
