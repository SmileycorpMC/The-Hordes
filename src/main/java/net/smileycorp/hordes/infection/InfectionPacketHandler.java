package net.smileycorp.hordes.infection;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.hordes.common.ModDefinitions;

public class InfectionPacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid + "_infection");
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientCureMessageHandler.class, SimpleStringMessage.class, 0, Side.CLIENT);
	}
	
	public static class ClientCureMessageHandler implements IMessageHandler<SimpleStringMessage, IMessage> {

		public ClientCureMessageHandler() {}

		@Override
		public IMessage onMessage(SimpleStringMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> {
					InfectionCureRegister.readPacketData(message.getText());
				});
			}
			return null;
		}
	}
}
