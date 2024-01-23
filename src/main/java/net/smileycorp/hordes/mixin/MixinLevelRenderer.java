package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.client.ClientConfigHandler;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow private Minecraft minecraft;

    @WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    public int renderSky$getMoonPhase(ClientLevel level, Operation<Integer> original) {
        if (!ClientConfigHandler.hordeEventTintsSky.get()) return original.call(level);
        LazyOptional<HordeEventClient> optional = minecraft.player.getCapability(HordesCapabilities.HORDE_EVENT_CLIENT);
        if (optional.isPresent() && optional.orElseGet(null).isHordeNight(level)) {
            Color rgb = ClientConfigHandler.getHordeMoonColour();
            RenderSystem.setShaderColor((float)rgb.getRed()/255f, (float)rgb.getGreen()/255f, (float)rgb.getBlue()/255f, 1f);
        }
        return original.call(level);
    }

}
