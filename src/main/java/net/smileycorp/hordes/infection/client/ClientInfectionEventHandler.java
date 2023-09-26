package net.smileycorp.hordes.infection.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;

import java.awt.*;

public class ClientInfectionEventHandler {

	@SubscribeEvent
	public void renderOverlay(CustomizeGuiOverlayEvent event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			if (player!= null) {
				if (player.hasEffect(HordesInfection.INFECTED.get())) {
					int level = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
					Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.01f*level);
					Window window = mc.getWindow();
					event.getGuiGraphics().fill(0, 0, window.getWidth(), window.getHeight(), colour.getRGB());
				}
			}
		}
	}

	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre event){
		LivingEntity entity = event.getEntity();
		Player player = Minecraft.getInstance().player;
		if (ClientConfigHandler.playerInfectionVisuals.get() && player != null && player.hasEffect(HordesInfection.INFECTED.get()) && entity != player) {
			int a = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
			if (a > 2) RenderSystem.setShaderColor(1, 0, 0, 1);
			else if (a == 2) RenderSystem.setShaderColor(1, 0.4f, 0.4f, 1);
		}
	}

	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post event){
		if (RenderSystem.getShaderColor().equals(new float[]{1, 1, 1, 1})) return;
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

}
