package net.smileycorp.hordes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
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
		if (CommonConfigHandler.piglinsHuntZombies.get()) callback.setReturnValue(false);
	}
	
	@Inject(at=@At("HEAD"), method = "isZombified", cancellable = true)
	private static void isZombified(EntityType<?> type, CallbackInfoReturnable<Boolean> callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get() && type.m_204039_(HordesInfection.INFECTION_ENTITIES_TAG)) callback.setReturnValue(true);
	}
	
	@Inject(at=@At("TAIL"), method = "findNearestValidAttackTarget", cancellable = true)
	private static void findNearestValidAttackTarget(Piglin piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> callback ) {
		if (!CommonConfigHandler.piglinsHuntZombies.get() || callback.getReturnValue().isPresent()) return;
		ItemStack stack = piglin.getItemInHand(InteractionHand.MAIN_HAND);
		if (stack == null) return;
		if (!stack.is(Items.CROSSBOW)) return;
		Brain<Piglin> brain = piglin.getBrain();
		if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) callback.setReturnValue(brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED));
	}
	
	@Inject(at=@At("HEAD"), method = "admireGoldItem", cancellable = true)
	private static void admireGoldItem(LivingEntity entity, CallbackInfo callback) {
		if (entity.hasEffect(HordesInfection.INFECTED.get()) && entity.getOffhandItem().m_204117_(HordesInfection.INFECTION_CURES_TAG)) entity.startUsingItem(InteractionHand.OFF_HAND);
	}

}
