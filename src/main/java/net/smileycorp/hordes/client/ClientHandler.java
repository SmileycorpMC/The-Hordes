package net.smileycorp.hordes.client;

import java.awt.Color;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.DrownedPlayer;
import net.smileycorp.hordes.common.entities.ZombiePlayer;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.network.CureEntityMessage;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
		EntityRenderers.register(HordesInfection.ZOMBIE_PLAYER.get(), m -> new ZombiePlayerRenderer<ZombiePlayer>(m, new Color(121, 156, 101)));
		EntityRenderers.register(HordesInfection.DROWNED_PLAYER.get(), m -> new ZombiePlayerRenderer<DrownedPlayer>(m, new Color(144, 255, 255)));
	}

	public static void playHordeSound(Vec3 dir, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			Level level = mc.level;
			Player player = mc.player;
			BlockPos pos = new BlockPos(player.getX() + (5*dir.x), player.getY(), player.getZ() + (5*dir.z));
			float pitch = 1+((level.random.nextInt(6)-3)/10);
			level.playSound(player, pos, new SoundEvent(sound), SoundSource.HOSTILE, 0.6f, pitch);
		}
	}

	public static void displayMessage(String text) {
		Gui gui = Minecraft.getInstance().gui;
		BaseComponent message = new TranslatableComponent(text);
		message.setStyle(Style.EMPTY.withColor(ClientConfigHandler.getHordeMessageColour()));
		if (ClientConfigHandler.eventNotifyMode.get() == 1) {
			gui.getChat().addMessage(message);
		} else if (ClientConfigHandler.eventNotifyMode.get() == 2) {
			gui.overlayMessageString=message;
			gui.overlayMessageTime=ClientConfigHandler.eventNotifyDuration.get();
			gui.animateOverlayMessageColor=false;
		} else if (ClientConfigHandler.eventNotifyMode.get() == 3) {
			gui.setTimes(5, ClientConfigHandler.eventNotifyDuration.get(), 5);
			gui.setTitle(new TextComponent(" "));
			gui.setSubtitle(message);
		}

	}

	public static void onInfect() {
		if (ClientConfigHandler.playerInfectSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			Level world = mc.level;
			Player player = mc.player;
			world.playSound(player, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1f, world.random.nextFloat());
		}
	}

	public static void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		Entity entity = message.getEntity(world);
		world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		Random rand = world.random;
		for (int i = 0; i < 10; ++i) {
			world.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
					entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
		}
	}

}
