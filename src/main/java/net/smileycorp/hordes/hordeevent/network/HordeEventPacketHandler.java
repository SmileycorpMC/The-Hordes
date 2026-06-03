package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.SimpleStringMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

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
		NETWORK_INSTANCE.registerMessage(HordeSoundMessage::process, HordeSoundMessage.class, 0, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(HordeEventPacketHandler::processNotificationMessage, SimpleStringMessage.class, 1, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(UpdateClientHordeMessage::process, UpdateClientHordeMessage.class, 2, Side.CLIENT);
	}

	public static IMessage processNotificationMessage(SimpleStringMessage message, MessageContext ctx) {
		if (ctx.side == Side.CLIENT) Minecraft.getMinecraft().addScheduledTask(() -> HordeClientHandler.INSTANCE.displayMessage(message.getText()));
		return null;
	}
	
}
