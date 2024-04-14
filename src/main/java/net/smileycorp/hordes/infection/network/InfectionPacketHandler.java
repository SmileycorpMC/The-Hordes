package net.smileycorp.hordes.infection.network;

import net.minecraft.network.Connection;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

import java.util.function.BiConsumer;

public class InfectionPacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;
	
	public static void sendTo(SimpleAbstractMessage message, Connection manager, NetworkDirection direction) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.sendTo(message, manager, direction);
	}
	
	public static void send(PacketDistributor.PacketTarget target, SimpleAbstractMessage message) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.send(target, message);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(Constants.loc("Infection"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(0, InfectMessage.class, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(InfectMessage.class), (msg, ctx) -> msg.process(ctx.get()));
		registerMessage(0, InfectMessage.class, InfectMessage::process);
		registerMessage(1, CureEntityMessage.class, CureEntityMessage::process);
		registerMessage(2, SyncImmunityItemsMessage.class, SyncImmunityItemsMessage::process);
		registerMessage(3, SyncWearableProtectionMessage.class, SyncWearableProtectionMessage::process);
	}
	
	private static <T extends SimpleAbstractMessage> void registerMessage(int id, Class<T> clazz, BiConsumer<T, Context> supplier) {
		NETWORK_INSTANCE.registerMessage(id, clazz, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(clazz), (msg, ctx) -> supplier.accept(msg, ctx.get()));
	}
	
}
