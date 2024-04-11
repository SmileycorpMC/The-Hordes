package net.smileycorp.hordes.infection.network;

import net.minecraft.network.Connection;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

public class InfectionPacketHandler {

	private static SimpleChannel NETWORK_INSTANCE;
	
	public static void sendTo(AbstractMessage message, Connection manager, NetworkDirection direction) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.sendTo(message, manager, direction);
	}
	
	public static void send(PacketDistributor.PacketTarget target, AbstractMessage message) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		NETWORK_INSTANCE.send(target, message);
	}

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("Infection"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 0, InfectMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 1, CureEntityMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 2, SyncImmunityItemsMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 3, SyncWearableProtectionMessage.class);
	}

}
