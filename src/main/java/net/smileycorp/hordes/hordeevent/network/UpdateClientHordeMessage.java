package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.smileycorp.hordes.client.ClientHandler;

public class UpdateClientHordeMessage implements IPacket<INetHandler> {

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
    public void read(PacketBuffer buf) {
        day = buf.readInt();
        day_length = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(day);
        buf.writeInt(day_length);
    }
    
    public void process(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setHordeDay(day, day_length)));
        ctx.setPacketHandled(true);
    }

    @Override
    public void handle(INetHandler p_131342_) {}

}
