package net.smileycorp.hordes.common.infection.network;

import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.hordes.common.Constants;

public class InfectionPacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("Infection"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 0, InfectMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 1, CureEntityMessage.class);
	}

}
