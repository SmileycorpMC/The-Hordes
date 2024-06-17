package net.smileycorp.hordes.infection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.smileycorp.hordes.common.Constants;

import java.util.Locale;

public class PotionHordes extends Potion {
    
    private final ResourceLocation texture;
    
    public PotionHordes(String name, boolean isBad, int colour) {
        super(isBad, colour);
        setPotionName("effect." + Constants.name(name));
        setRegistryName(Constants.loc(name));
        texture = Constants.loc("textures/mob_effect/" + name.toLowerCase(Locale.US) + ".png");
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        renderEffect(x + 6, y + 7, 1);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        renderEffect(x + 3, y + 3, alpha);
    }
    
    @SideOnly(Side.CLIENT)
    private void renderEffect(int x, int y, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, alpha);
        Minecraft.getMinecraft().renderEngine.bindTexture(getTexture());
        Gui.drawScaledCustomSizeModalRect(x, y, 0, 0 , 18, 18, 18, 18, 18, 18);
        GlStateManager.popMatrix();
    }
    
    protected ResourceLocation getTexture() {
        return texture;
    }
    
}
