package net.smileycorp.hordes.infection.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.hordes.config.InfectionConfig;

public class InfectionPacketHandler {
	
	public static void sendTo(CustomPacketPayload message, ServerPlayer player) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		PacketDistributor.sendToPlayer(player, message);
	}
	
	public static void sendTracking(CustomPacketPayload message, Entity entity) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		PacketDistributor.sendToPlayersTrackingEntity(entity, message);
	}
	
	public static void sendToServer(CustomPacketPayload message) {
		if (!InfectionConfig.enableMobInfection.get()) return;
		PacketDistributor.sendToServer(message);
	}
	
	public static void initPackets(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar channel = event.registrar("1");
		NetworkUtils.register(channel, InfectMessage.TYPE, InfectMessage.class);
		NetworkUtils.register(channel, CureEntityMessage.TYPE, CureEntityMessage.class);
		NetworkUtils.register(channel, SyncImmunityItemsMessage.TYPE, SyncImmunityItemsMessage.class);
		NetworkUtils.register(channel, SyncWearableProtectionMessage.TYPE, SyncWearableProtectionMessage.class);
	}

}
