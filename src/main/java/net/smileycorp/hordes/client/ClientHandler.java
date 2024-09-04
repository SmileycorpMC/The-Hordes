package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.smileycorp.hordes.client.render.ZombiePlayerRenderer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.entities.HordesEntities;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.infection.client.ClientInfectionEventHandler;
import net.smileycorp.hordes.infection.network.CureEntityMessage;

import java.awt.*;
import java.util.Random;


@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
		RenderingRegistry.registerEntityRenderingHandler(HordesEntities.ZOMBIE_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getZombiePlayerColour(), Constants.loc("textures/entity/layer/zombie_player_outer_layer.png"), false, false));
		RenderingRegistry.registerEntityRenderingHandler(HordesEntities.DROWNED_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getDrownedPlayerColour(), Constants.loc("textures/entity/layer/drowned_player_outer_layer.png"), true, false));
		RenderingRegistry.registerEntityRenderingHandler(HordesEntities.HUSK_PLAYER.get(), ctx -> new ZombiePlayerRenderer<>(ctx,
				ClientConfigHandler.getHuskPlayerColour(), Constants.loc("textures/entity/layer/husk_player_outer_layer.png"), false, true));
	}
	
	@SubscribeEvent
	public void renderNameplate(RenderNameplateEvent event) {
		if (event.getEntity() instanceof PlayerZombie) {
			event.setContent(event.getEntity().getCustomName());
		}
	}
	
	@SubscribeEvent
	public void fogColour(EntityViewRenderEvent.FogColors event) {
		if (!ClientConfigHandler.hordeEventTintsSky.get()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld level = mc.level;
		LazyOptional<HordeEventClient> optional = mc.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
		if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level)) {
			float d = level.getSkyDarken((float)event.getRenderPartialTicks()) * 1.4f;
			Color rgb = ClientConfigHandler.getHordeSkyColour();
			event.setRed((1f - d) * (float)rgb.getRed()/255f + (d * event.getRed()));
			event.setGreen((1f - d) * (float)rgb.getGreen()/255f + d * event.getGreen());
			event.setBlue((1f - d) * (float)rgb.getBlue()/255f + d * event.getBlue());
		}
	}
	
	public static void playHordeSound(Vector3d vec3, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld level = mc.level;
			ClientPlayerEntity player = mc.player;
			BlockPos pos = new BlockPos(player.getX() + (10 * vec3.x), player.getY(), player.getZ() + (10 * vec3.z));
			float pitch = 1 + ((level.random.nextInt(6) - 3) / 10);
			level.playSound(player, pos, new SoundEvent(sound), SoundCategory.HOSTILE, 0.5f, pitch);
		}
	}
	
	public static void setHordeDay(int day, int day_length) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		LazyOptional<HordeEventClient> optional = player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
		if (optional.isPresent()) optional.orElseGet(null).setNextDay(day, day_length);
	}
	
	public static void displayMessage(String text) {
		Minecraft mc = Minecraft.getInstance();
		IngameGui gui = mc.gui;
		TextComponent message = new TranslationTextComponent(text);
		message.setStyle(Style.EMPTY.withColor(net.minecraft.util.text.Color.fromRgb(ClientConfigHandler.getHordeMessageColour())));
		if (ClientConfigHandler.eventNotifyMode.get() == 1) gui.getChat().addMessage(message);
		else if (ClientConfigHandler.eventNotifyMode.get() == 2) {
			gui.overlayMessageString = message;
			gui.overlayMessageTime = ClientConfigHandler.eventNotifyDuration.get();
			gui.animateOverlayMessageColor = false;
		} else if (ClientConfigHandler.eventNotifyMode.get() == 3)
			gui.setTitles(message, null, 5, ClientConfigHandler.eventNotifyDuration.get(), 5);
	}
	
	public static void onInfect(boolean prevented) {
		if (ClientConfigHandler.playerInfectSound.get() &! prevented) {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld level = mc.level;
			if (level == null) return;
			ClientPlayerEntity player = mc.player;
			level.playSound(player, player.blockPosition(), Constants.INFECT_SOUND, SoundCategory.PLAYERS, 0.75f, level.random.nextFloat());
		}
		if (ClientConfigHandler.infectionProtectSound.get() && prevented) {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld level = mc.level;
			if (level == null) return;
			ClientPlayerEntity player = mc.player;
			level.playSound(player, player.blockPosition(), Constants.IMMUNE_SOUND, SoundCategory.PLAYERS, 0.75f, level.random.nextFloat());
		}
	}
	
	public static void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld level = mc.level;
		Entity entity = message.getEntity(level);
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		Random rand = level.random;
		for (int i = 0; i < 10; ++i) level.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
				entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
	}
	
}
