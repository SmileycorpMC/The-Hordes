package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

public class ClientHandler {

	public static void playHordeSound(Vec3d dir) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		EntityPlayer player = mc.player;
		BlockPos pos = new BlockPos(player.posX + (5*dir.x), player.posY, player.posZ + (5*dir.z));
		float pitch = 1+((world.rand.nextInt(6)-3)/10);
		world.playSound(player, pos, new SoundEvent(ModDefinitions.getResource("horde_spawn")), SoundCategory.HOSTILE, 0.3f, pitch);
	}

	public static EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}

	public static void displayMessage(String text) {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		ITextComponent message = new TextComponentTranslation(text);
		message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
		if (ConfigHandler.eventNotifyMode == 1) {
			gui.addChatMessage(ChatType.CHAT, message);
		} else if (ConfigHandler.eventNotifyMode == 2) {
			gui.overlayMessage=message.getFormattedText();
			gui.overlayMessageTime=ConfigHandler.eventNotifyDuration;
			gui.animateOverlayMessageColor=false;
		} else if (ConfigHandler.eventNotifyMode == 3) {
			gui.displayTitle(null, null, 5, ConfigHandler.eventNotifyDuration, 5);
			gui.displayTitle(" ", null, 0, 0, 0);
			gui.displayTitle(null, message.getFormattedText(), 0, 0, 0);
		}
		
	}

	public static void onInfect() {
		if (ConfigHandler.playerInfectSound) {
			Minecraft mc = Minecraft.getMinecraft();
			World world = mc.world;
			EntityPlayer player = mc.player;
			world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 0.75f, world.rand.nextFloat());
		}
	}

}
