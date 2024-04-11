package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.smileycorp.atlas.api.util.TextUtils;
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

@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
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
		if (event.getEntity() instanceof PlayerZombie) {
			event.setContent(event.getEntity().getCustomName());
		}
	}

	@SubscribeEvent
	public void fogColour(ViewportEvent.ComputeFogColor event) {
		if (!ClientConfigHandler.hordeEventTintsSky.get()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LazyOptional<HordeEventClient> optional = mc.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
		if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level)) {
			float d = level.getSkyDarken((float)event.getPartialTick()) * 1.4f;
			Color rgb = ClientConfigHandler.getHordeSkyColour();
			event.setRed((1f - d) * (float)rgb.getRed()/255f + (d * event.getRed()));
			event.setGreen((1f - d) * (float)rgb.getGreen()/255f + d * event.getGreen());
			event.setBlue((1f - d) * (float)rgb.getBlue()/255f + d * event.getBlue());
		}
	}

	public static void playHordeSound(Vec3 vec3, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			LocalPlayer player = mc.player;
			BlockPos pos = BlockPos.containing(player.getX() + (10 * vec3.x), player.getY(), player.getZ() + (10 * vec3.z));
			float pitch = 1 + ((level.random.nextInt(6) - 3) / 10);
			level.playSound(player, pos, SoundEvent.createVariableRangeEvent(sound), SoundSource.HOSTILE, 0.5f, pitch);
		}
	}

	public static void setHordeDay(int day, int day_length) {
		LocalPlayer player = Minecraft.getInstance().player;
		LazyOptional<HordeEventClient> optional = player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
		if (optional.isPresent()) optional.orElseGet(null).setNextDay(day, day_length);
	}

	public static void displayMessage(String text) {
		Minecraft mc = Minecraft.getInstance();
		Gui gui = mc.gui;
		MutableComponent message = TextUtils.translatableComponent(text, null);
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
