package net.smileycorp.hordes.common.infection.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.infection.InfectionRegister;

public class InfectionPacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(Constants.loc("Infection"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(1, InfectMessage.class, new SimpleMessageEncoder<InfectMessage>(),
				new SimpleMessageDecoder<>(InfectMessage.class), (T, K)-> processInfectMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(2, CureEntityMessage.class, new SimpleMessageEncoder<CureEntityMessage>(),
				new SimpleMessageDecoder<>(CureEntityMessage.class), (T, K)-> processCureEntityMessage(T, K.get()));
	}

	private static void processInfectMessage(InfectMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.onInfect()));
		ctx.setPacketHandled(true);
	}

	private static void processCureEntityMessage(CureEntityMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processCureEntity(message)));
		ctx.setPacketHandled(true);
	}

}
