package net.smileycorp.hordes.client;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;

import org.lwjgl.opengl.GL11;

public class ClientInfectionEventHandler {
	
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Pre event){
		if (ConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player!= null && event.getType() == ElementType.POTION_ICONS) {
				if (player.isPotionActive(HordesInfection.INFECTED)) {
					int level = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
			    	Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.05f*level*level);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDepthMask(false);
			        GL11.glDisable(GL11.GL_ALPHA_TEST);
			        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			    	Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, colour.getRGB());
			    	GL11.glDepthMask(true);
			        GL11.glEnable(GL11.GL_DEPTH_TEST);
			        GL11.glEnable(GL11.GL_ALPHA_TEST);
				}
			}
		}
	}
	
}
