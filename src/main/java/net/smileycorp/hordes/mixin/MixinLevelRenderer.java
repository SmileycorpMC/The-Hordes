package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.client.HordeClientHandler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.awt.*;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow private Minecraft minecraft;
    
    @Shadow @Nullable private ClientLevel level;
    
    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    public void renderSky$getMoonPhase(Matrix4f pProjectionMatrix, Matrix4f pFrustrumMatrix, float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup, CallbackInfo ci) {
        if (!ClientConfigHandler.hordeEventTintsSky.get() |! HordeClientHandler.INSTANCE.isHordeNight(level)) return;
        Color rgb = ClientConfigHandler.getHordeMoonColour();
        RenderSystem.setShaderColor((float)rgb.getRed()/255f, (float)rgb.getGreen()/255f, (float)rgb.getBlue()/255f, 1f);
    }

}
