package net.smileycorp.hordes.infection.network;

import net.minecraft.network.Connection;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

public class InfectionPacketHandler {

	private static SimpleChannel NETWORK_INSTANCE;
	
	public static void sendTo(SimpleAbstractMessage message, Connection manager, NetworkDirection direction) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.sendTo(message, manager, direction);
	}
	
	public static void send(PacketDistributor.PacketTarget target, SimpleAbstractMessage message) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.send(target, message);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("Infection"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 0, InfectMessage.class, (msg, ctx) -> msg.process(ctx.get()));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 1, CureEntityMessage.class, (msg, ctx) -> msg.process(ctx.get()));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 2, SyncImmunityItemsMessage.class, (msg, ctx) -> msg.process(ctx.get()));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 3, SyncWearableProtectionMessage.class, (msg, ctx) -> msg.process(ctx.get()));
	}

}
