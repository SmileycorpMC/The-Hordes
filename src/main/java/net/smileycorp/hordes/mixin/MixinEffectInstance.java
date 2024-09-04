package net.smileycorp.hordes.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkDirection;
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

@Mixin(EffectInstance.class)
public class MixinEffectInstance {

	@Shadow
	private Effect effect;

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
				if (entity instanceof ServerPlayerEntity) InfectionPacketHandler.sendTo(new InfectMessage(false),
						((ServerPlayerEntity) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
				callback.setReturnValue(true);
			}
			else {
				entity.hurt(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
				callback.setReturnValue(false);
			}
		}
	}


	@Inject(at=@At("TAIL"), method = "load", cancellable = true)
	private static void load(CompoundNBT nbt, CallbackInfoReturnable<EffectInstance> callback) {
		EffectInstance effect = callback.getReturnValue();
		if (effect.getEffect() == HordesInfection.INFECTED.get()) {
			if (effect.duration > InfectionConfig.ticksForEffectStage.get()) {
				int d = effect.duration + InfectionConfig.ticksForEffectStage.get() - 10000;
				if (d > 0) effect.duration = d;
				else effect.duration = InfectionConfig.ticksForEffectStage.get();
			}
		}
	}

}