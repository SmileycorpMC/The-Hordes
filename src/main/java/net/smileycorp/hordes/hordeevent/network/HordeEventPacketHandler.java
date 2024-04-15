package net.smileycorp.hordes.hordeevent.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.function.Supplier;

public class HordeEventPacketHandler {

	private static SimpleChannel NETWORK_INSTANCE;
	
	public static void sendTo(IPacket<INetHandler> message, NetworkManager manager, NetworkDirection direction) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		NETWORK_INSTANCE.sendTo(message, manager, direction);
	}
	
	public static void send(PacketDistributor.PacketTarget target, IPacket<INetHandler> message) {
		if (!HordeEventConfig.enableHordeEvent.get()) return;
		NETWORK_INSTANCE.send(target, message);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(Constants.loc("HordeEvent"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(0, HordeSoundMessage.class, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(HordeSoundMessage.class), (T, K)-> T.process(K.get()));
		NETWORK_INSTANCE.registerMessage(1, SimpleStringMessage.class, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(SimpleStringMessage.class), HordeEventPacketHandler::processNotificationMessage);
		NETWORK_INSTANCE.registerMessage(2, UpdateClientHordeMessage.class, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(UpdateClientHordeMessage.class), (T, K)-> T.process(K.get()));
	}

	public static void processNotificationMessage(SimpleStringMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.displayMessage(message.getText())));
		ctx.get().setPacketHandled(true);
	}

}
