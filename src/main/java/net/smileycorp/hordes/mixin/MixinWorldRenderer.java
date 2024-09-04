package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
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

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow private Minecraft minecraft;
    
    @Shadow @Nullable private ClientWorld level;
    
    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getMoonPhase()I"))
    public void renderSky$getMoonPhase(MatrixStack p_228424_1_, float p_228424_2_, CallbackInfo ci) {
        if (!ClientConfigHandler.hordeEventTintsSky.get()) return;
        LazyOptional<HordeEventClient> optional = minecraft.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
        if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level)) {
            Color rgb = ClientConfigHandler.getHordeMoonColour();
            GlStateManager._color4f((float)rgb.getRed()/255f, (float)rgb.getGreen()/255f, (float)rgb.getBlue()/255f, 1f);
        }
    }

}
