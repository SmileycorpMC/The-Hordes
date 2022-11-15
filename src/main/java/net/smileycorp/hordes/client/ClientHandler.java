package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.network.CureEntityMessage;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
	}

	@SubscribeEvent
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(HordesInfection.ZOMBIE_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx, ClientConfigHandler.getZombiePlayerColour()));
		event.registerEntityRenderer(HordesInfection.DROWNED_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx, ClientConfigHandler.getDrownedPlayerColour()));
	}

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ZombiePlayerRenderer.DEFAULT, () -> ZombiePlayerRenderer.createLayer(false));
		event.registerLayerDefinition(ZombiePlayerRenderer.SLIM, () -> ZombiePlayerRenderer.createLayer(true));
	}

	@SubscribeEvent
	public void renderNameplate(RenderNameTagEvent event) {
		if (event.getEntity() instanceof IZombiePlayer) {
			event.setContent(event.getEntity().getCustomName());
		}
	}

	public static void playHordeSound(Vec3 vec3, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			Player player = mc.player;
			BlockPos pos = new BlockPos(player.getX() + (5*vec3.x), player.getY(), player.getZ() + (5*vec3.z));
			float pitch = 1+((level.random.nextInt(6)-3)/10);
			level.playSound(player, pos, new SoundEvent(sound), SoundSource.HOSTILE, 0.6f, pitch);
		}
	}

	public static void displayMessage(String text) {
		Minecraft mc = Minecraft.getInstance();
		Gui gui = mc.gui;
		MutableComponent message = MutableComponent.create(new TranslatableContents(text));
		message.setStyle(Style.EMPTY.withColor(ClientConfigHandler.getHordeMessageColour()));
		if (ClientConfigHandler.eventNotifyMode.get() == 1) {
			gui.getChat().addMessage(message);
		} else if (ClientConfigHandler.eventNotifyMode.get() == 2) {
			gui.overlayMessageString = message;
			gui.overlayMessageTime = ClientConfigHandler.eventNotifyDuration.get();
			gui.animateOverlayMessageColor = false;
		} else if (ClientConfigHandler.eventNotifyMode.get() == 3) {
			gui.setTimes(5, ClientConfigHandler.eventNotifyDuration.get(), 5);
			gui.setSubtitle(message);
		}

	}

	public static void onInfect() {
		if (ClientConfigHandler.playerInfectSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			Player player = mc.player;
			level.playSound(player, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1f, level.random.nextFloat());
		}
	}

	public static void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		Entity entity = message.getEntity(level);
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		RandomSource rand = level.random;
		for (int i = 0; i < 10; ++i) {
			level.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
					entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
		}
	}

}
