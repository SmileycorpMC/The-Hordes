package net.smileycorp.hordes.infection.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.smileycorp.hordes.infection.HordesInfection;

import java.awt.*;

public class InfectionLayer implements LayeredDraw.Layer {
    
    @Override
    public void render(GuiGraphics gui, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!player.hasEffect(HordesInfection.INFECTED)) return;
        int a = player.getEffect(HordesInfection.INFECTED).getAmplifier();
        if (a == 0) return;
        Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.01f * a);
        Window window = mc.getWindow();
        gui.fill(0, 0, window.getWidth(), window.getHeight(), colour.getRGB());
    }
    
}
