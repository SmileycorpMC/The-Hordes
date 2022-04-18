package net.smileycorp.hordes.common.infection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;

public class InfectMessage extends SimpleAbstractMessage {

	public InfectMessage(){}

	@Override
	public void read(FriendlyByteBuf buf) {}

	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public void handle(PacketListener handler) {}

}