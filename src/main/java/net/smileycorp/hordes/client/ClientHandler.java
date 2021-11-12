package net.smileycorp.hordes.client;

import java.awt.Color;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.DrownedPlayerEntity;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.network.CureEntityMessage;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
		MinecraftForge.EVENT_BUS.register(new ClientInfectionEventHandler());
		RenderingRegistry.registerEntityRenderingHandler(HordesInfection.ZOMBIE_PLAYER.get(), m -> new ZombiePlayerRenderer<ZombiePlayerEntity>(m, new Color(121, 156, 101)));
		RenderingRegistry.registerEntityRenderingHandler(HordesInfection.DROWNED_PLAYER.get(), m -> new ZombiePlayerRenderer<DrownedPlayerEntity>(m, new Color(144, 255, 255)));
	}

	public static void playHordeSound(Vector3d dir, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			World world = mc.level;
			PlayerEntity player = mc.player;
			BlockPos pos = new BlockPos(player.getX() + (5*dir.x), player.getY(), player.getZ() + (5*dir.z));
			float pitch = 1+((world.random.nextInt(6)-3)/10);
			world.playSound(player, pos, new SoundEvent(sound), SoundCategory.HOSTILE, 0.6f, pitch);
		}
	}

	public static void displayMessage(String text) {
		IngameGui gui = Minecraft.getInstance().gui;
		TextComponent message = new TranslationTextComponent(text);
		message.setStyle(Style.EMPTY.withColor(ClientConfigHandler.getHordeMessageColour()));
		if (ClientConfigHandler.eventNotifyMode.get() == 1) {
			gui.getChat().addMessage(message);
		} else if (ClientConfigHandler.eventNotifyMode.get() == 2) {
			gui.overlayMessageString=message;
			gui.overlayMessageTime=ClientConfigHandler.eventNotifyDuration.get();
			gui.animateOverlayMessageColor=false;
		} else if (ClientConfigHandler.eventNotifyMode.get() == 3) {
			gui.setTitles(null, null, 5, ClientConfigHandler.eventNotifyDuration.get(), 5);
			gui.setTitles(new StringTextComponent(" "), null, 0, 0, 0);
			gui.setTitles(null, message, 0, 0, 0);
		}

	}

	public static void onInfect() {
		if (ClientConfigHandler.playerInfectSound.get()) {
			Minecraft mc = Minecraft.getInstance();
			World world = mc.level;
			PlayerEntity player = mc.player;
			world.playSound(player, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 1f, world.random.nextFloat());
		}
	}

	public static void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		World world = mc.level;
		Entity entity = message.getEntity(world);
		world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		Random rand = world.random;
		for (int i = 0; i < 10; ++i) {
			world.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
					entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
		}
	}

}
