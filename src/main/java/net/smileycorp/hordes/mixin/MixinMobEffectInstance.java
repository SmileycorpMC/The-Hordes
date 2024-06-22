package net.smileycorp.hordes.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectedEffect;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public class MixinMobEffectInstance {

	@Shadow
	private MobEffect effect;

	@Shadow
	private int duration;

	@Shadow
	private int amplifier;

	@Inject(at=@At("HEAD"), method = "tick", cancellable = true)
	public void tick(LivingEntity entity, Runnable onUpdate, CallbackInfoReturnable<Boolean> callback) {
		if (duration <= 1 && effect == HordesInfection.INFECTED.get() && InfectionConfig.enableMobInfection.get()) {
			if (amplifier < 3) {
				amplifier = amplifier + 1;
				duration = InfectedEffect.getInfectionTime(entity);
				if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(false), (ServerPlayer) entity);
				callback.setReturnValue(true);
			}
			else {
				entity.hurt(HordesInfection.getInfectionDamage(entity), Float.MAX_VALUE);
				callback.setReturnValue(false);
			}
		}
	}

}
