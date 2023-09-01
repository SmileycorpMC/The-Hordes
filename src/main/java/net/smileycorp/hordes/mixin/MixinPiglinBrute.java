package net.smileycorp.hordes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBrute.class)
public abstract class MixinPiglinBrute extends AbstractPiglin {

	@Shadow public abstract Brain<Piglin> getBrain();

	protected MixinPiglinBrute(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "customServerAiStep()V", cancellable = true)
	public void customServerAiStep(CallbackInfo callback) {
		if (!(CommonConfigHandler.enableMobInfection.get() && CommonConfigHandler.piglinsCureThemself.get())) return;
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if(!getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)) return;
		if (!getOffhandItem().isEmpty()) return;
		ItemStack stack = new ItemStack(Items.GOLDEN_APPLE);
		if (stack.is(HordesInfection.INFECTION_CURES_TAG)) {
			if (getOffhandItem().isEmpty()) {
				setItemInHand(InteractionHand.OFF_HAND, stack);
				startUsingItem(InteractionHand.OFF_HAND);
			}
		}
	}

}
