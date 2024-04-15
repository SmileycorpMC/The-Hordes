package net.smileycorp.hordes.infection.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

import java.util.function.BiConsumer;

public class InfectionPacketHandler {
	
	public static SimpleChannel NETWORK_INSTANCE;
	
	public static void sendTo(IPacket<INetHandler> message, NetworkManager manager, NetworkDirection direction) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.sendTo(message, manager, direction);
	}
	
	public static void send(PacketDistributor.PacketTarget target, IPacket<INetHandler> message) {
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
	
	private static <T extends IPacket<INetHandler>> void registerMessage(int id, Class<T> clazz, BiConsumer<T, NetworkEvent.Context> supplier) {
		NETWORK_INSTANCE.registerMessage(id, clazz, new SimpleMessageEncoder<>(),
				new SimpleMessageDecoder<>(clazz), (msg, ctx) -> supplier.accept(msg, ctx.get()));
	}
	
}