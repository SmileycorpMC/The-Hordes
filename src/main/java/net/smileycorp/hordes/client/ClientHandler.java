package net.smileycorp.hordes.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.smileycorp.hordes.client.render.ZombiePlayerRenderer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.config.ClientConfigHandler;

public class ClientHandler {
	
	@SubscribeEvent
	public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(HordesEntities.ZOMBIE_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getZombiePlayerColour(), Constants.loc("textures/entity/layer/zombie_player_outer_layer.png"), false, false));
		event.registerEntityRenderer(HordesEntities.DROWNED_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getDrownedPlayerColour(), Constants.loc("textures/entity/layer/drowned_player_outer_layer.png"), true, false));
		event.registerEntityRenderer(HordesEntities.HUSK_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getHuskPlayerColour(), Constants.loc("textures/entity/layer/husk_player_outer_layer.png"), false, true));
	}

	@SubscribeEvent
	public void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ZombiePlayerRenderer.DEFAULT, () -> ZombiePlayerRenderer.createLayer(false));
		event.registerLayerDefinition(ZombiePlayerRenderer.SLIM, () -> ZombiePlayerRenderer.createLayer(true));
	}
	
	public static void renderNameplate(RenderNameTagEvent event) {
		if (event.getEntity() instanceof PlayerZombie) event.setContent(event.getEntity().getCustomName());
	}
	
}
