package net.smileycorp.hordes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    
    @Shadow private Minecraft mc;
    
    @Shadow @Nullable private WorldClient world;
    
    @Inject(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getMoonPhase()I"))
    public void renderSky$getMoonPhase(float partialTicks, int pass, CallbackInfo callback) {
        if (!ClientConfigHandler.hordeEventTintsSky) return;
        HordeEventClient horde = mc.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT, null);
        if (horde != null && horde.isHordeNight(world)) {
            int[] rgb = ClientConfigHandler.getHordeMoonColour();
            GlStateManager.color((float)rgb[0]/255f, (float)rgb[1]/255f, (float)rgb[2]/255f, 1f);
        }
    }
    
}
