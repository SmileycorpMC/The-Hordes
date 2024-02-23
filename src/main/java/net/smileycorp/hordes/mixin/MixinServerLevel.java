package net.smileycorp.hordes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.smileycorp.hordes.config.HordeEventConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    @Shadow public abstract List<ServerPlayer> players();

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(BooleanSupplier bool, CallbackInfo callback) {
        if (HordeEventConfig.pauseEventServer.get() && players().isEmpty()) callback.cancel();
    }

}
