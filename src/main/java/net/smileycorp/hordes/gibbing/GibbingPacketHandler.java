package net.smileycorp.hordes.gibbing;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.hordes.common.ModDefinitions;

public class GibbingPacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientSyncHandler.class, BodyPartMessage.class, 0, Side.CLIENT);
	}
	
	public static class ClientSyncHandler implements IMessageHandler<BodyPartMessage, IMessage> {

		public ClientSyncHandler() {}

		@Override
		public IMessage onMessage(BodyPartMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				
				mc.addScheduledTask(() -> {
				World world = mc.world;
				Entity entity = world.getEntityByID(message.getEntityID());
				IZombieGibbing gibbing = entity.getCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP);
				ZombieGibbingProvider.GIBBING.getStorage().readNBT(ZombieGibbingProvider.GIBBING, gibbing, EnumFacing.UP, message.getNBT());
				
				});
			}
			return null;
		}
	}
}
