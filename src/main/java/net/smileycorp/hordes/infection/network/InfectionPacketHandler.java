package net.smileycorp.hordes.infection.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

public class InfectionPacketHandler {

	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MODID + "_infection");
	
	public static void sendTo(IMessage message, EntityPlayerMP playerMP) {
		if (!InfectionConfig.enableMobInfection) return;
		NETWORK_INSTANCE.sendTo(message, playerMP);
	}
	
	public static void send(Entity entity, IMessage message) {
		if (!InfectionConfig.enableMobInfection) return;
		NETWORK_INSTANCE.sendToAllTracking(message, entity);
	}
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(SyncCuresMessage::process, SyncCuresMessage.class, 0, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(InfectMessage::process, InfectMessage.class, 1, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(CureEntityMessage::process, CureEntityMessage.class, 2, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(SyncImmunityItemsMessage::process, SyncImmunityItemsMessage.class, 3, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(SyncWearableProtectionMessage::process, SyncWearableProtectionMessage.class, 4, Side.CLIENT);
	}
	
}
