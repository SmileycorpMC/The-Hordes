package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
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
    
    @Inject(method = "m_202423_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    public void renderSky$getMoonPhase(PoseStack p_202424_, Matrix4f p_202425_, float p_202426_, Camera p_202427_, boolean p_202428_, Runnable p_202429_, CallbackInfo ci) {
        if (!ClientConfigHandler.hordeEventTintsSky.get()) return;
        LazyOptional<HordeEventClient> optional = minecraft.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
        if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level)) {
            Color rgb = ClientConfigHandler.getHordeMoonColour();
            RenderSystem.setShaderColor((float)rgb.getRed()/255f, (float)rgb.getGreen()/255f, (float)rgb.getBlue()/255f, 1f);
        }
    }

}
