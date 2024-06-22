package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.hordes.client.render.ZombiePlayerRenderer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;
import net.smileycorp.hordes.infection.network.CureEntityMessage;

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		NeoForge.EVENT_BUS.register(new ClientHandler());
		NeoForge.EVENT_BUS.register(new ClientInfectionEventHandler());
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
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
	
	public static void onInfect(boolean prevented) {
		if (ClientConfigHandler.playerInfectSound.get() &! prevented) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			LocalPlayer player = mc.player;
			level.playSound(player, player.blockPosition(), Constants.INFECT_SOUND, SoundSource.PLAYERS, 0.75f, level.random.nextFloat());
		}
		if (ClientConfigHandler.infectionProtectSound.get() && prevented) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			LocalPlayer player = mc.player;
			level.playSound(player, player.blockPosition(), Constants.IMMUNE_SOUND, SoundSource.PLAYERS, 0.75f, level.random.nextFloat());
		}
	}

	public static void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		Entity entity = message.getEntity(level);
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		RandomSource rand = level.random;
		for (int i = 0; i < 10; ++i) level.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
					entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
	}

}
