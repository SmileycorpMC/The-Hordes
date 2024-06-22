package net.smileycorp.hordes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class MixinDamageSource {

    @Inject(at = @At("HEAD"), method = "getLocalizedDeathMessage", cancellable = true)
    public void getLocalizedDeathMessage(LivingEntity entity, CallbackInfoReturnable<Component> callback) {
        if (!(entity instanceof Player && this.equals(HordesInfection.getInfectionDamage(entity)))) return;
        String msg = "death.attack.infection";
        ZombifyPlayer cap = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER);
        if (cap != null && cap.wasZombified()) msg += ".zombified";
        callback.setReturnValue(Component.translatable(msg, entity.getDisplayName()));
    }

}
