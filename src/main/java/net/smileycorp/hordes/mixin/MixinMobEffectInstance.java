package net.smileycorp.hordes.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionRegister;
import net.smileycorp.hordes.common.infection.network.InfectMessage;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;
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

	@Inject(at=@At("HEAD"), method = "tick(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z", cancellable = true)
	public void tick(LivingEntity entity, Runnable onUpdate, CallbackInfoReturnable<Boolean> callback) {
		if (duration <= 1 && effect == HordesInfection.INFECTED.get()) {
			if (amplifier < 3) {
				amplifier = amplifier + 1;
				duration = InfectionRegister.getInfectionTime(entity);
				if (entity instanceof ServerPlayer) InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), ((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
				callback.setReturnValue(true);
				callback.cancel();
				return;
			}
			else {
				entity.hurt(HordesInfection.getInfectionDamage(entity), Float.MAX_VALUE);
				callback.setReturnValue(false);
				callback.cancel();
				return;
			}
		}
	}


	@Inject(at=@At("TAIL"), method = "load(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/effect/MobEffectInstance;", cancellable = true)
	private static void load(CompoundTag nbt, CallbackInfoReturnable<MobEffectInstance> callback) {
		MobEffectInstance effect = callback.getReturnValue();
		if (effect.getEffect() == HordesInfection.INFECTED.get()) {
			if (effect.duration > CommonConfigHandler.ticksForEffectStage.get()) {
				int d = effect.duration + CommonConfigHandler.ticksForEffectStage.get() - 10000;
				if (d > 0) effect.duration = d;
				else effect.duration = CommonConfigHandler.ticksForEffectStage.get();
			}
		}
	}

}
