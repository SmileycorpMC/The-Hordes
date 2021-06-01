package net.smileycorp.hordes.common.hordeevent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeEventPacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientSyncHandler.class, HordeSoundMessage.class, 0, Side.CLIENT);
	}
	
	public static class ClientSyncHandler implements IMessageHandler<HordeSoundMessage, IMessage> {

		public ClientSyncHandler() {}

		@Override
		public IMessage onMessage(HordeSoundMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				
				mc.addScheduledTask(() -> {
					World world = mc.world;
					EntityPlayer player = mc.player;
					float pitch = 1+((world.rand.nextInt(6)-3)/10);
					world.playSound(player, player.getPosition(), new SoundEvent(ModDefinitions.getResource("horde_spawn")), SoundCategory.HOSTILE, 0.3f, pitch);
				
				});
			}
			return null;
		}
	}
}
