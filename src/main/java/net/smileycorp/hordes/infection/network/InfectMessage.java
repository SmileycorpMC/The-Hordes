package net.smileycorp.hordes.infection.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.client.ClientHandler;

public class InfectMessage implements IMessage {
    
    private boolean prevented;
    
    public InfectMessage() {}
    
    public InfectMessage(boolean prevented) {
        this.prevented = prevented;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        prevented = buf.readBoolean();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(prevented);
    }
    
    public IMessage process(MessageContext ctx) {
        if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> ClientHandler.onInfect(prevented));
        return null;
    }
    
}
