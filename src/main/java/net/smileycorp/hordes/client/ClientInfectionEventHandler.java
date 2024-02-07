package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ClientInfectionEventHandler {

	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre<EntityLivingBase> event){
		if (ConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player.isPotionActive(HordesInfection.INFECTED) && event.getEntity() != player) {
				if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier()>2) {
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.SET);
					GlStateManager.color(1, 0, 0);
				} else if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier() == 2) {
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.EQUIV);
					GlStateManager.color(1, 0.4f, 0.4f);
				}
			}
		}
	}

	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post<EntityLivingBase> event){
		if (ConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player.isPotionActive(HordesInfection.INFECTED) && event.getEntity() != player) {
				if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier() >= 2) {
					GlStateManager.color(1, 1, 1);
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.AND);
				}
			}
		}
	}

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event){
		if (ConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player!= null && event.getType() == ElementType.VIGNETTE) {
				if (player.isPotionActive(HordesInfection.INFECTED)) {
					int level = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
					Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.04f*level*level);
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
