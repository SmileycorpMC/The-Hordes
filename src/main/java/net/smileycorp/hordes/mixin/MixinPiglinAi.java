package net.smileycorp.hordes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PiglinAi.class)
public abstract class MixinPiglinAi {

	@Inject(at=@At("HEAD"), method = "isNearZombified(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Z", cancellable = true)
	private static void isNearZombified(Piglin piglin, CallbackInfoReturnable<Boolean> callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get()) {
			callback.setReturnValue(false);
			callback.cancel();
		}
	}

	@Inject(at=@At("HEAD"), method = "isZombified(Lnet/minecraft/world/entity/EntityType;)Z", cancellable = true)
	private static void isZombified(EntityType<?> p_34807_, CallbackInfoReturnable<Boolean> callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get() && InfectionRegister.canCauseInfection(p_34807_)) {
			callback.setReturnValue(true);
			callback.cancel();
		}
	}

	@Inject(at=@At("TAIL"), method = "findNearestValidAttackTarget(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/Optional;", cancellable = true)
	private static void findNearestValidAttackTarget(Piglin piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> callback ) {
		if (CommonConfigHandler.piglinsHuntZombies.get()) {
			if (callback.getReturnValue().isEmpty()) {
				ItemStack stack = piglin.getItemInHand(InteractionHand.MAIN_HAND);
				if (stack != null) {
					if (stack.is(Items.CROSSBOW)) {
						Brain<Piglin> brain = piglin.getBrain();
						if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
							callback.setReturnValue(brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED));
							callback.cancel();
						}
					}
				}
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "admireGoldItem(Lnet/minecraft/world/entity/LivingEntity;)V", cancellable = true)
	private static void admireGoldItem(LivingEntity entity, CallbackInfo callback) {
		if (entity.hasEffect(HordesInfection.INFECTED.get()) && entity.getOffhandItem().m_204117_(HordesInfection.INFECTION_CURES_TAG)) {
			entity.startUsingItem(InteractionHand.OFF_HAND);
			callback.cancel();
		}
	}

}
