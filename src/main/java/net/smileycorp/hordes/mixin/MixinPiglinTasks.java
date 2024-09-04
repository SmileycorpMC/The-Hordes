package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PiglinTasks.class)
public abstract class MixinPiglinTasks {
	
	@Inject(at=@At("HEAD"), method = "isNearZombified", cancellable = true)
	private static void isNearZombified(PiglinEntity piglin, CallbackInfoReturnable<Boolean> callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get()) callback.setReturnValue(false);
	}
	
	@Inject(at=@At("HEAD"), method = "isZombified", cancellable = true)
	private static void isZombified(EntityType<?> type, CallbackInfoReturnable<Boolean> callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get() && type.is(HordesInfection.INFECTION_ENTITIES_TAG)) callback.setReturnValue(true);
	}
	
	@Inject(at=@At("TAIL"), method = "findNearestValidAttackTarget", cancellable = true)
	private static void findNearestValidAttackTarget(PiglinEntity piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> callback ) {
		if (!CommonConfigHandler.piglinsHuntZombies.get() || callback.getReturnValue().isPresent()) return;
		ItemStack stack = piglin.getItemInHand(Hand.MAIN_HAND);
		if (stack == null) return;
		if (stack.getItem() != Items.CROSSBOW) return;
		Brain<PiglinEntity> brain = piglin.getBrain();
		if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) callback.setReturnValue(brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED));
	}
	
	@Inject(at=@At("HEAD"), method = "admireGoldItem", cancellable = true)
	private static void admireGoldItem(LivingEntity entity, CallbackInfo callback) {
		if (entity.hasEffect(HordesInfection.INFECTED.get()) && entity.getOffhandItem().getItem().is(HordesInfection.INFECTION_CURES_TAG)) entity.startUsingItem(Hand.OFF_HAND);
	}

}
