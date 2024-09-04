package net.smileycorp.hordes.mixin;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.hordes.config.HordeEventConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(BooleanSupplier bool, CallbackInfo callback) {
        if (HordeEventConfig.pauseEventServer.get() && ServerLifecycleHooks.getCurrentServer().getPlayerCount() <= 0) callback.cancel();
    }

}
