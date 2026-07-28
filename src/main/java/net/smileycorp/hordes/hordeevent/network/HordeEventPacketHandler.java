package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.atlas.api.network.GenericStringMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;

import java.util.function.Supplier;

public class HordeEventPacketHandler {

	private static SimpleChannel NETWORK_INSTANCE;

	public static void sendTo(AbstractMessage message, ServerPlayer player) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		NETWORK_INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void send(PacketDistributor.PacketTarget target, AbstractMessage message) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		NETWORK_INSTANCE.send(target, message);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("HordeEvent"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE,0, HordeSoundMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE,1, GenericStringMessage.class, HordeEventPacketHandler::processNotificationMessage);
		NetworkUtils.registerMessage(NETWORK_INSTANCE,2, UpdateClientHordeMessage.class);
	}

	public static void processNotificationMessage(GenericStringMessage message, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> HordeClientHandler.INSTANCE.displayMessage(message.getText())));
		ctx.get().setPacketHandled(true);
	}

}
