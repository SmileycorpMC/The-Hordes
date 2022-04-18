package net.smileycorp.hordes.common.hordeevent.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeEventPacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(ModDefinitions.getResource("HordeEvent"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(0, HordeSoundMessage.class, new SimpleMessageEncoder<HordeSoundMessage>(),
				new SimpleMessageDecoder<HordeSoundMessage>(HordeSoundMessage.class), (T, K)-> processSoundMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(1, SimpleStringMessage.class, new SimpleMessageEncoder<SimpleStringMessage>(),
				new SimpleMessageDecoder<SimpleStringMessage>(SimpleStringMessage.class), (T, K)-> processNotificationMessage(T, K.get()));
	}

	public static void processSoundMessage(HordeSoundMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.playHordeSound(message.getDirection(), message.getSound())));
		ctx.setPacketHandled(true);
	}

	public static void processNotificationMessage(SimpleStringMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.displayMessage(message.getText())));
		ctx.setPacketHandled(true);
	}
}
