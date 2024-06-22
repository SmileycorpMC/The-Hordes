package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.smileycorp.atlas.api.network.GenericStringMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.client.HordeEventClient;

public class HordeEventPacketHandler {
	
	public static final CustomPacketPayload.Type<GenericStringMessage> NOTIFICATION = new CustomPacketPayload.Type(Constants.loc("notification"));
	
	public static void sendTo(CustomPacketPayload message, ServerPlayer player) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		PacketDistributor.sendToPlayer(player, message);
	}
	
	public static void send(CustomPacketPayload message) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		PacketDistributor.sendToServer(message);
	}

	public static void initPackets(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar channel = event.registrar("1");
		NetworkUtils.register(channel,HordeSoundMessage.TYPE, HordeSoundMessage.class);
		NetworkUtils.register(channel, NOTIFICATION, GenericStringMessage.class, HordeEventPacketHandler::processNotificationMessage);
		NetworkUtils.register(channel, UpdateClientHordeMessage.TYPE, UpdateClientHordeMessage.class);
	}

	public static void processNotificationMessage(GenericStringMessage message, IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> HordeEventClient.INSTANCE.displayMessage(message.getText()));
	}

}
