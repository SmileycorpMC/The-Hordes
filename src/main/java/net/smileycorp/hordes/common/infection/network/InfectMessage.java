package net.smileycorp.hordes.common.infection.network;

import java.io.IOException;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class InfectMessage implements IPacket<INetHandler> {

	public InfectMessage(){}

	@Override
	public void read(PacketBuffer buf) throws IOException {}

	@Override
	public void write(PacketBuffer buf) throws IOException {}

	@Override
	public void handle(INetHandler handler) {}

}