package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.HordeEventConfig;

public class UpdateClientHordeMessage extends SimpleAbstractMessage {

    private boolean horde_day;
    private int day_length;

    public UpdateClientHordeMessage() {}

    public UpdateClientHordeMessage(boolean horde_day) {
        this.horde_day = horde_day;
        this.day_length = HordeEventConfig.dayLength.get();
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        horde_day = buf.readBoolean();
        day_length = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(horde_day);
        buf.writeInt(day_length);
    }
    
    public void process(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setHordeDay(horde_day, day_length)));
        ctx.setPacketHandled(true);
    }

    @Override
    public void handle(PacketListener p_131342_) {}

}
