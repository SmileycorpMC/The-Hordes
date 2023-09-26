package net.smileycorp.hordes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.IZombifyPlayer;
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
        LazyOptional<IZombifyPlayer> optional = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER);
        if (optional.isPresent() && optional.resolve().get().wasZombified()) msg += ".zombified";
        callback.setReturnValue(Component.translatable(msg, entity.getDisplayName()));
    }

}
