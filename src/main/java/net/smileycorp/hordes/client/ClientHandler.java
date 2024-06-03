package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.infection.CureEntityMessage;

import java.util.Random;

public class ClientHandler {
	
	@SubscribeEvent
	public void fogColour(EntityViewRenderEvent.FogColors event) {
		if (!ClientConfigHandler.hordeEventTintsSky) return;
		Minecraft mc = Minecraft.getMinecraft();
		WorldClient world = mc.world;
		HordeEventClient horde = mc.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT, null);
		if (horde != null && horde.isHordeNight(world)) {
			float d = world.getSunBrightnessBody((float)event.getRenderPartialTicks()) * 1.4f;
			int[] rgb = ClientConfigHandler.getHordeSkyColour();
			event.setRed((1f - d) * (float)rgb[0]/255f + (d * event.getRed()));
			event.setGreen((1f - d) * (float)rgb[1]/255f + d * event.getGreen());
			event.setBlue((1f - d) * (float)rgb[2]/255f + d * event.getBlue());
		}
	}

	public static void playHordeSound(Vec3d dir, ResourceLocation sound) {
		if (ClientConfigHandler.hordeSpawnSound) {
			Minecraft mc = Minecraft.getMinecraft();
			World world = mc.world;
			EntityPlayer player = mc.player;
			BlockPos pos = new BlockPos(player.posX + (10 * dir.x), player.posY, player.posZ + (10 * dir.z));
			float pitch = 1 + ((world.rand.nextInt(6) - 3) / 10);
			world.playSound(player, pos, new SoundEvent(sound), SoundCategory.HOSTILE, 0.5f, pitch);
		}
	}
	
	public static void setHordeDay(int day, int day_length) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player.hasCapability(HordesCapabilities.HORDE_EVENT_CLIENT, null))
			player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT, null).setNextDay(day, day_length);
	}

	public static void displayMessage(String text) {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		ITextComponent message = new TextComponentTranslation(text);
		message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
		if (ClientConfigHandler.eventNotifyMode == 1) {
			gui.addChatMessage(ChatType.CHAT, message);
		} else if (ClientConfigHandler.eventNotifyMode == 2) {
			gui.overlayMessage=message.getFormattedText();
			gui.overlayMessageTime= ClientConfigHandler.eventNotifyDuration;
			gui.animateOverlayMessageColor=false;
		} else if (ClientConfigHandler.eventNotifyMode == 3) {
			gui.displayTitle(null, null, 5, ClientConfigHandler.eventNotifyDuration, 5);
			gui.displayTitle(" ", null, 0, 0, 0);
			gui.displayTitle(null, message.getFormattedText(), 0, 0, 0);
		}

	}
	
	public static void onInfect(boolean prevented) {
		if (ClientConfigHandler.playerInfectSound &! prevented) {
			Minecraft mc = Minecraft.getMinecraft();
			WorldClient level = mc.world;
			if (level == null) return;
			EntityPlayerSP player = mc.player;
			level.playSound(player, player.getPosition(), Constants.INFECT_SOUND, SoundCategory.PLAYERS, 0.75f, level.rand.nextFloat());
		}
		if (ClientConfigHandler.infectionProtectSound && prevented) {
			Minecraft mc = Minecraft.getMinecraft();
			WorldClient level = mc.world;
			if (level == null) return;
			EntityPlayerSP player = mc.player;
			level.playSound(player, player.getPosition(), Constants.IMMUNE_SOUND, SoundCategory.PLAYERS, 0.75f, level.rand.nextFloat());
		}
	}

	public static void processCureEntityMessage(CureEntityMessage message) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		Entity entity = message.getEntity(world);
		world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, entity.getSoundCategory(), 1f, 1f, true);
		Random rand = world.rand;
		for (int i = 0; i < 10; ++i) {
			world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, entity.posX + (rand.nextDouble() - 0.5D) * entity.width * 1.5,
					entity.posY + rand.nextDouble() * entity.height, entity.posZ + (rand.nextDouble() - 0.5D) * entity.width * 1.5, 0.0D, 0.3D, 0.0D);
		}
	}

}
