package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow private Minecraft minecraft;

    @Shadow private ClientLevel level;

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    public int renderSky$getMoonPhase(ClientLevel level) {
        LazyOptional<HordeEventClient> optional = minecraft.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
        if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level))
            RenderSystem.setShaderColor(0.7647f, 0.1451f, 0.0314f, 1);
        return level.getMoonPhase();
    }

}
