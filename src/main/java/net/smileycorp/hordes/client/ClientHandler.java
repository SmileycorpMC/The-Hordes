package net.smileycorp.hordes.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.smileycorp.hordes.client.render.ZombiePlayerRenderer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;
import net.smileycorp.hordes.infection.client.InfectionClientHandler;

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(HordeClientHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(InfectionClientHandler.INSTANCE);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(HordesEntities.ZOMBIE_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getZombiePlayerColour(), Constants.loc("textures/entity/layer/zombie_player_outer_layer.png"), false, false));
		event.registerEntityRenderer(HordesEntities.DROWNED_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getDrownedPlayerColour(), Constants.loc("textures/entity/layer/drowned_player_outer_layer.png"), true, false));
		event.registerEntityRenderer(HordesEntities.HUSK_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getHuskPlayerColour(), Constants.loc("textures/entity/layer/husk_player_outer_layer.png"), false, true));
	}

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ZombiePlayerRenderer.DEFAULT, () -> ZombiePlayerRenderer.createLayer(false));
		event.registerLayerDefinition(ZombiePlayerRenderer.SLIM, () -> ZombiePlayerRenderer.createLayer(true));
	}
	
	@SubscribeEvent
	public void renderNameplate(RenderNameTagEvent event) {
		if (event.getEntity() instanceof PlayerZombie) event.setContent(event.getEntity().getCustomName());
	}

}
