package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeEventPacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientSyncHandler.class, HordeSoundMessage.class, 0, Side.CLIENT);
	}
	
	public static class ClientSyncHandler implements IMessageHandler<HordeSoundMessage, IMessage> {

		public ClientSyncHandler() {}

		@Override
		public IMessage onMessage(HordeSoundMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				
				mc.addScheduledTask(() -> {
					ClientHandler.playHordeSound(message.getDirection());
				
				});
			}
			return null;
		}
	}
}
