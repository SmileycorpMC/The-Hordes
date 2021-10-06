package net.smileycorp.hordes.infection;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.ModDefinitions;

public class InfectionPacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid + "_infection");
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientCureMessageHandler.class, SimpleStringMessage.class, 0, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(InfectMessageHandler.class, InfectMessage.class, 1, Side.CLIENT);
	}
	
	public static class ClientCureMessageHandler implements IMessageHandler<SimpleStringMessage, IMessage> {

		public ClientCureMessageHandler() {}

		@Override
		public IMessage onMessage(SimpleStringMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> {
					InfectionRegister.readCurePacketData(message.getText());
				});
			}
			return null;
		}
	}
	
	public static class InfectMessageHandler implements IMessageHandler<InfectMessage, IMessage> {

		public InfectMessageHandler() {}

		@Override
		public IMessage onMessage(InfectMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> {
					ClientHandler.onInfect();
				});
			}
			return null;
		}
	}
	
	public static class InfectMessage implements IMessage {
		
		public InfectMessage(){}
		
		@Override
		public void fromBytes(ByteBuf buf) {}
	
		@Override
		public void toBytes(ByteBuf buf) {}
	}
}
