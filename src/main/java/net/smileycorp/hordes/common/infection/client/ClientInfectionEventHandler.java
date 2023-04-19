package net.smileycorp.hordes.common.infection.client;

import java.awt.Color;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;

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
					GuiComponent.fill(event.getPoseStack(), 0, 0, window.getWidth(), window.getHeight(), colour.getRGB());
				}
			}
		}
	}

}
