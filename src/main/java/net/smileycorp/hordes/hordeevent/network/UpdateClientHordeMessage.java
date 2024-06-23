package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

public class UpdateClientHordeMessage implements NetworkMessage {
    
    public static Type<UpdateClientHordeMessage> TYPE = new Type(Constants.loc("sync_horde_client"));
    
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

    @Override
    public void process(IPayloadContext ctx) {
       if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> HordeClientHandler.INSTANCE.setHordeDay(horde_day, day_length));
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
