package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;

public class HordeEventPacketHandler {

	public static SimpleNetworkWrapper NETWORK_INSTANCE;
	
	public static void sendTo(IMessage message, EntityPlayerMP playerMP) {
		if (!HordeEventConfig.enableHordeEvent) return;
		NETWORK_INSTANCE.sendTo(message, playerMP);
	}
	
	public static void send(Entity entity, IMessage message) {
		if (!HordeEventConfig.enableHordeEvent) return;
		NETWORK_INSTANCE.sendToAllTracking(message, entity);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MODID +"_hordeevent");
		NETWORK_INSTANCE.registerMessage(SoundMessageHandler.class, HordeSoundMessage.class, 0, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(NotificationMessageHandler.class, SimpleStringMessage.class, 1, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(UpdateClientHordeHandler.class, UpdateClientHordeMessage.class, 2, Side.CLIENT);
	}

	public static class SoundMessageHandler implements IMessageHandler<HordeSoundMessage, IMessage> {

		public SoundMessageHandler() {}

		@Override
		public IMessage onMessage(HordeSoundMessage message, MessageContext ctx) {

			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();

				mc.addScheduledTask(() -> {
					ClientHandler.playHordeSound(message.getDirection(), message.getSound());

				});
			}
			return null;
		}
	}

	public static class NotificationMessageHandler implements IMessageHandler<SimpleStringMessage, IMessage> {

		public NotificationMessageHandler() {}

		@Override
		public IMessage onMessage(SimpleStringMessage message, MessageContext ctx) {

			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();

				mc.addScheduledTask(() -> {
					ClientHandler.displayMessage(message.getText());

				});
			}
			return null;
		}
	}
	
	public static class UpdateClientHordeHandler implements IMessageHandler<UpdateClientHordeMessage, IMessage> {
		
		public UpdateClientHordeHandler() {}
		
		@Override
		public IMessage onMessage(UpdateClientHordeMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> message.process());
			}
			return null;
		}
	}
	
}
